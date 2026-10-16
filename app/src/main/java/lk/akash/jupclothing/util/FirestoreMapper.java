package lk.akash.jupclothing.util;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lk.akash.jupclothing.model.Category;
import lk.akash.jupclothing.model.Product;

public final class FirestoreMapper {
    private FirestoreMapper() {}

    public static Category toCategory(DocumentSnapshot doc) {
        Category c;
        try { c = doc.toObject(Category.class); } catch (RuntimeException ex) { c = new Category(); }
        if (c == null) c = new Category();

        c.setDocumentId(doc.getId());
        if (blank(c.getCategory())) {
            String value = valueAsString(first(doc, "category", "categoryId", "id", "slug"));
            c.setCategory(blank(value) ? doc.getId() : value);
        }
        if (blank(c.getName())) c.setName(valueAsString(first(doc, "name", "title", "categoryName", "label")));
        if (blank(c.getImageUrl())) c.setImageUrl(firstImage(doc, "imageUrl", "imageURL", "image", "photoUrl", "photo", "thumbnail"));
        return c;
    }

    public static Product toProduct(DocumentSnapshot doc) {
        Product p;
        try { p = doc.toObject(Product.class); } catch (RuntimeException ex) { p = new Product(); }
        if (p == null) p = new Product();

        if (blank(p.getProductId())) {
            String value = valueAsString(first(doc, "productId", "id", "productID"));
            p.setProductId(blank(value) ? doc.getId() : value);
        }
        if (blank(p.getTitle())) p.setTitle(valueAsString(first(doc, "title", "name", "productName")));
        if (blank(p.getDescription())) p.setDescription(valueAsString(first(doc, "description", "desc")));
        if (blank(p.getCategoryId())) {
            p.setCategoryId(valueAsString(first(doc, "categoryId", "category", "categoryID", "category_id", "categoryRef")));
        }

        Number price = number(doc, "price", "productPrice", "unitPrice");
        if (price != null) p.setPrice(price.doubleValue());
        Number stock = number(doc, "stockCount", "stock", "quantity", "qty", "availableQty");
        if (stock != null) p.setStockCount(stock.intValue());

        Object status = first(doc, "status", "active", "isActive");
        if (status instanceof Boolean) p.setStatus((Boolean) status);

        if (p.getImages() == null || p.getImages().isEmpty()) {
            List<String> images = new ArrayList<>();
            collectImages(first(doc, "images", "imageUrls", "imageURLS", "imageUrl", "imageURL", "image", "photoUrl", "photos", "thumbnail"), images);
            p.setImages(images);
        } else {
            List<String> cleaned = new ArrayList<>();
            for (String image : p.getImages()) addImage(image, cleaned);
            p.setImages(cleaned);
        }
        if (p.getImages() == null) p.setImages(Collections.emptyList());
        return p;
    }

    /** Match old/new Firestore category schemas without requiring an index. */
    public static boolean matchesCategory(DocumentSnapshot productDoc, String... categoryCandidates) {
        Object raw = first(productDoc, "categoryId", "category", "categoryID", "category_id", "categoryRef", "categoryName");
        List<String> productValues = new ArrayList<>();
        collectComparableValues(raw, productValues);
        if (productValues.isEmpty()) {
            Product p = toProduct(productDoc);
            if (!blank(p.getCategoryId())) productValues.add(p.getCategoryId());
        }
        for (String candidate : categoryCandidates) {
            if (blank(candidate)) continue;
            String normalizedCandidate = normalize(candidate);
            for (String value : productValues) {
                if (normalizedCandidate.equals(normalize(value))) return true;
                if (normalizedCandidate.equals(normalize(lastPathPart(value)))) return true;
            }
        }
        return false;
    }

    public static String string(DocumentSnapshot doc, String... keys) {
        return valueAsString(first(doc, keys));
    }

    private static Number number(DocumentSnapshot doc, String... keys) {
        Object value = first(doc, keys);
        if (value instanceof Number) return (Number) value;
        if (value != null) {
            try { return Double.parseDouble(value.toString().replace(",", "").trim()); }
            catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static Object first(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value != null) return value;
        }
        return null;
    }

    private static String firstImage(DocumentSnapshot doc, String... keys) {
        List<String> images = new ArrayList<>();
        collectImages(first(doc, keys), images);
        return images.isEmpty() ? "" : images.get(0);
    }

    @SuppressWarnings("unchecked")
    private static void collectImages(Object value, List<String> out) {
        if (value == null) return;
        if (value instanceof String) {
            addImage((String) value, out);
            return;
        }
        if (value instanceof DocumentReference) {
            return;
        }
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) collectImages(item, out);
            return;
        }
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            String[] preferred = {"url", "imageUrl", "imageURL", "downloadUrl", "src", "path"};
            for (String key : preferred) {
                if (map.containsKey(key)) collectImages(map.get(key), out);
            }
            if (out.isEmpty()) {
                for (Object item : map.values()) collectImages(item, out);
            }
        }
    }

    private static void addImage(String raw, List<String> out) {
        if (raw == null) return;
        String value = raw.trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1).trim();
        }
        if (!value.isEmpty() && !out.contains(value)) out.add(value);
    }

    private static void collectComparableValues(Object value, List<String> out) {
        if (value == null) return;
        if (value instanceof DocumentReference) {
            DocumentReference ref = (DocumentReference) value;
            out.add(ref.getId());
            out.add(ref.getPath());
            return;
        }
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) collectComparableValues(item, out);
            return;
        }
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            String[] keys = {"id", "categoryId", "category", "name", "title"};
            for (String key : keys) if (map.containsKey(key)) collectComparableValues(map.get(key), out);
            return;
        }
        out.add(String.valueOf(value));
    }

    private static String valueAsString(Object value) {
        if (value == null) return "";
        if (value instanceof DocumentReference) return ((DocumentReference) value).getId();
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            String[] keys = {"id", "categoryId", "category", "name", "title", "value"};
            for (String key : keys) {
                if (map.get(key) != null) return String.valueOf(map.get(key));
            }
        }
        return String.valueOf(value);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replace(" ", "").replace("_", "").replace("-", "");
    }

    private static String lastPathPart(String s) {
        if (s == null) return "";
        int slash = s.lastIndexOf('/');
        return slash >= 0 && slash < s.length() - 1 ? s.substring(slash + 1) : s;
    }

    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}
