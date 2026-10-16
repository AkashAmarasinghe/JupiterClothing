
<div align="center">

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&height=230&color=0:0B0F19,50:991B1B,100:F59E0B&text=JUPITER%20CLOTHING&fontSize=46&fontColor=FFFFFF&fontAlignY=39&animation=fadeIn&desc=Android%20E-Commerce%20Application&descAlignY=61&descSize=19" alt="Jupiter Clothing Header"/>

<br/>

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
<img src="https://img.shields.io/badge/Payments-PayHere-2563EB?style=for-the-badge"/>
<img src="https://img.shields.io/badge/IDE-Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white"/>

<br/><br/>

**A native Android shopping application built with Java, XML, and Firebase.**

Jupiter Clothing provides a convenient mobile shopping experience, allowing customers to explore clothing products, manage their shopping cart, save favorite products, and proceed through checkout.

<br/>

[![Repository](https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github)](https://github.com/AkashAmarasinghe/JupiterClothing)
[![Source Code](https://img.shields.io/badge/View-Source%20Code-F59E0B?style=for-the-badge&logo=github&logoColor=black)](https://github.com/AkashAmarasinghe/JupiterClothing/tree/main/app/src/main)

</div>

---

## 📌 Project Overview

**Jupiter Clothing** is a native Android e-commerce application developed as part of a Software Engineering academic project.

The application focuses on providing a user-friendly shopping experience through an Android mobile interface.

Customers can register, sign in, browse available products, view product details, manage a shopping cart and wishlist, and access payment functionality.

Firebase services are used for authentication and cloud-based data management.

The project demonstrates Android application development, Firebase integration, asynchronous operations, navigation, and e-commerce workflows.

---

## ✨ Key Features

### 🔐 User Authentication

- User registration and login
- Firebase Authentication integration
- User session management
- Customer profile management

### 🛍️ Product Browsing

- Product listing
- Category-based product browsing
- Product details and pricing
- Product images
- Quantity selection
- Product search functionality

### 🛒 Shopping Cart

- Add products to the cart
- View selected products
- Manage product quantities
- Remove cart items
- Review order information
- Proceed to checkout

### ❤️ Wishlist

- Add favorite products
- View saved products
- Manage wishlist items

### 💳 Checkout and Payments

- Checkout interface
- Customer order information
- PayHere payment SDK integration
- Payment result handling
- Payment confirmation screen

> Payment processing depends on valid PayHere merchant configuration and the availability of the payment service. Use sandbox credentials for development and testing.

### 👤 Customer Profile

- View customer information
- Edit profile details
- Select a profile picture
- Update profile information
- Firebase-based profile data management

### 🔔 Notifications

- Customer notification interface
- Display notification messages
- Notification read/unread management
- Firebase integration

### 📦 Orders

- Order-related data handling
- Order information screens
- Payment and order result interfaces

> Complete end-to-end order tracking and delivery updates may require additional backend configuration and testing.

---

## 🛠️ Technology Stack

<div align="center">

| Technology | Purpose |
|:---:|:---|
| **Java** | Android application logic |
| **XML** | User interface layouts |
| **Android SDK** | Android application development |
| **Android Studio** | Development environment |
| **Firebase Authentication** | User registration and authentication |
| **Cloud Firestore** | Cloud database |
| **Firebase Storage** | Image storage integration |
| **PayHere SDK** | Payment integration |
| **Gradle** | Build and dependency management |
| **Git & GitHub** | Version control |

<br/>

<img src="https://skillicons.dev/icons?i=java,androidstudio,android,firebase,gradle,git,github&theme=dark" alt="Technology Stack"/>

</div>

---

## 📱 Application Screenshots

<div align="center">

### Authentication and Home

| Login | Home | Products |
|:---:|:---:|:---:|
| <img src="screenshots/login.png" width="220" alt="Login Screen"/> | <img src="screenshots/home.png" width="220" alt="Home Screen"/> | <img src="screenshots/products.png" width="220" alt="Products Screen"/> |

### Shopping Experience

| Cart | Checkout | Payment |
|:---:|:---:|:---:|
| <img src="screenshots/cart.png" width="220" alt="Cart Screen"/> | <img src="screenshots/checkout.png" width="220" alt="Checkout Screen"/> | <img src="screenshots/payment.png" width="220" alt="Payment Screen"/> |

### Account Management

| Profile | Notifications |
|:---:|:---:|
| <img src="screenshots/profile.png" width="220" alt="Profile Screen"/> | <img src="screenshots/notifications.png" width="220" alt="Notifications Screen"/> |

</div>

> Screenshots will appear after the corresponding image files are added to the `screenshots/` directory.

---

## 🏗️ Application Architecture

The application follows an Android component-based structure using Activities, Fragments, Adapters, Models, and utility classes.

```text
Android Application
        |
        v
Activities / Fragments
        |
        v
Adapters / Models / Utilities
        |
        v
Firebase Services
        |
        +-- Firebase Authentication
        |
        +-- Cloud Firestore
        |
        +-- Image Storage Integration

Payment Flow
        |
        v
PayHere Android SDK
        |
        v
Payment Result Handling
```

### Main Components

**Activities**

Handle application screens, navigation, and user interactions.

**Fragments**

Represent reusable sections of the user interface.

**RecyclerView and Adapters**

Display collections such as products, categories, cart items, and notifications.

**Models**

Represent application data structures such as users, products, orders, and cart items.

**Firebase**

Provides authentication and cloud-based data access.

---

## 📂 Project Structure

```text
JupiterClothing/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── lk/akash/jupclothing/
│   │   │   │       ├── activity/
│   │   │   │       ├── adapter/
│   │   │   │       ├── fragment/
│   │   │   │       ├── model/
│   │   │   │       └── util/
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   ├── layout/
│   │   │   │   ├── menu/
│   │   │   │   ├── mipmap/
│   │   │   │   └── values/
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── androidTest/
│   │   └── test/
│   │
│   └── build.gradle
│
├── gradle/
├── screenshots/
├── .gitignore
├── build.gradle
├── settings.gradle
├── gradle.properties
├── firestore.rules
├── storage.rules
├── gradlew
├── gradlew.bat
└── README.md
```

---

## ⚙️ Installation and Setup

### Prerequisites

Before running this project, make sure you have:

- Android Studio
- Android SDK
- JDK compatible with the project's Gradle configuration
- A Firebase project
- Internet connectivity
- An Android emulator or physical Android device

### 1. Clone the Repository

```bash
git clone https://github.com/AkashAmarasinghe/JupiterClothing.git
```

### 2. Open the Project

1. Launch Android Studio.
2. Select **Open**.
3. Navigate to the cloned `JupiterClothing` folder.
4. Wait for Gradle synchronization.
5. Install any missing Android SDK components when requested.

### 3. Configure Firebase

1. Open the Firebase Console.
2. Create or select a Firebase project.
3. Register an Android application matching the project's application ID.
4. Download the `google-services.json` configuration file.
5. Place it in the `app/` directory.
6. Enable the required Firebase services.
7. Configure appropriate database and storage security rules.

```text
JupiterClothing/
└── app/
    └── google-services.json
```

**Important:** Never add Firebase Admin service-account private keys, passwords, or other private credentials to the repository.

### 4. Configure Local SDK Settings

Android Studio normally generates `local.properties` automatically.

If needed, configure your local Android SDK path in that file.

Do not commit machine-specific local configuration files.

### 5. Configure Payment Integration

The application includes PayHere payment integration.

To use the payment functionality:

1. Configure the required PayHere merchant settings.
2. Use sandbox credentials during development.
3. Review the payment integration configuration.
4. Test success, failure, and cancellation cases.
5. Verify successful payments securely before fulfilling real orders.

### 6. Run the Application

1. Connect an Android phone or start an emulator.
2. Enable USB debugging if using a physical device.
3. Select the application run configuration.
4. Click **Run** in Android Studio.

---

## 🔄 Shopping Workflow

```text
Launch Application
        |
        v
Register / Login
        |
        v
Home Screen
        |
        v
Browse Categories
        |
        v
Select Product
        |
        v
Product Details
        |
        +----> Add to Wishlist
        |
        v
Add to Cart
        |
        v
Review Cart
        |
        v
Checkout
        |
        v
PayHere Payment
        |
        v
Payment Result
        |
        v
Order Information
```

---

## 🔒 Security Considerations

- Firebase Authentication is used for user authentication.
- Firestore Security Rules should restrict data access appropriately.
- Administrative database writes should require verified administrator privileges.
- Firebase Storage rules should enforce ownership and upload restrictions.
- Payment credentials and private merchant secrets must not be stored in client-side source code.
- Backend verification is recommended before treating a payment as successfully completed.
- Review project configuration and commit history before deploying publicly.

---

## 🚀 Future Improvements

- Complete end-to-end order tracking
- Improved product filtering
- Enhanced delivery management
- Stronger server-side payment verification
- Improved image caching and performance
- Expanded automated testing
- Improved accessibility and responsiveness

---

## 📚 Learning Outcomes

This project provided practical experience with:

- Native Android development using Java
- XML-based Android user interfaces
- Activities and Fragments
- RecyclerView and Adapters
- Firebase Authentication
- Cloud Firestore integration
- Asynchronous operations
- Payment SDK integration
- Mobile application debugging
- Git and GitHub version control

---

## 👨‍💻 Developer

<div align="center">

<img src="https://capsule-render.vercel.app/api?type=rect&height=100&color=0:111827,100:991B1B&text=AKASH%20AMARASINGHE&fontSize=27&fontColor=FFFFFF&fontAlignY=50" width="100%" alt="Developer Banner"/>

**Akash Amarasinghe**

Software Engineering Undergraduate | Full-Stack Development

<br/>

[![GitHub](https://img.shields.io/badge/GitHub-AkashAmarasinghe-181717?style=for-the-badge&logo=github)](https://github.com/AkashAmarasinghe)

</div>

---

<div align="center">

### ⭐ Support This Project

If you find this project useful, consider giving the repository a star!

**Developed with Java, Android Studio, and Firebase.**

<img src="https://capsule-render.vercel.app/api?type=waving&height=110&section=footer&color=0:0B0F19,50:991B1B,100:F59E0B" width="100%" alt="Footer"/>

</div>
