JClothing Firebase fix
======================
The Android app is already configured for Firebase project: jclothing-8a30b
Package: lk.akash.jupclothing

IMPORTANT: PERMISSION_DENIED is returned by Firebase servers. Android code cannot override server-side Firestore rules.
To make the existing database load, publish the included firestore.rules once:
Firebase Console > Firestore Database > Rules > replace with firestore.rules > Publish.

If product/category image fields use Firebase Storage (gs:// URLs), also publish storage.rules at:
Firebase Console > Storage > Rules > replace with storage.rules > Publish.

No database recreation is required. Keep your existing collections/data.
