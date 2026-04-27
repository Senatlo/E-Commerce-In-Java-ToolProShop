# ToolProShop - Java E-Commerce Desktop Application

ToolProShop is a Java desktop application that simulates an online shopping system for tools. It allows customers to log in, browse products, add items to a shopping cart, apply discounts, and complete checkout through a graphical user interface.

The project also includes an admin dashboard where administrators can manage products, view users and orders, update discount settings, and review basic analytics.

---

## Features

### Customer Features

- Customer login
- Browse available tool products
- View product details and images
- Add products to the shopping cart
- Increase, decrease, or remove cart items
- Automatic discount checking
- Checkout with receipt generation
- Order storage in the database and text files

### Admin Features

- Admin login
- Add, update, and delete products
- Manage product stock, price, image, and details
- View registered users
- View completed orders
- Manage discount settings
- View basic analytics through the admin dashboard

---

## Object-Oriented Programming Concepts Used

This project demonstrates several important Object-Oriented Programming concepts:

- **Abstraction**: `Product` is an abstract base class for all products.
- **Inheritance**: `PhysicalProduct` extends the `Product` class.
- **Polymorphism**: Product types can share a common parent while still having their own behavior.
- **Encapsulation**: Product, user, cart, and order data are protected using private fields and methods.
- **Exception Handling**: Custom exceptions are used for cases such as out-of-stock products.
- **Custom Data Structure**: The shopping cart uses a custom linked list instead of Java’s built-in list classes.
- **Database Access**: DAO classes handle database operations for products, users, orders, and discounts.
- **File Storage**: Completed orders are also saved as text files.

---

## Technologies Used

- Java
- Java Swing
- JDBC
- H2 Database
- File I/O
- Object-Oriented Programming
- Custom Linked List

---

## Project Structure

```text
E-Commerce-In-Java-ToolProShop/
│
├── assets/                 # Product and application images
├── orders/                 # Text-file copies of completed orders
├── out/                    # Compiled output files
├── src/com/shopping/
│   │
│   ├── cart/               # Shopping cart and cart item classes
│   ├── dao/                # Database access classes
│   ├── event/              # Event handling classes
│   ├── exception/          # Custom exception classes
│   ├── gui/                # Main Swing GUI
│   ├── model/              # Product, user, and role models
│   ├── order/              # Order processing and file storage
│   ├── util/               # Database connection and custom utilities
│   └── Main.java           # Application entry point
│
├── h2.jar                  # H2 database library
├── sources.txt             # Source file list
├── LICENSE
└── README.md
```

---

## Main Classes

### `Product`

`Product` is the abstract base class for products in the system. It stores shared product details such as ID, name, price, stock quantity, and image path.

### `PhysicalProduct`

`PhysicalProduct` represents the tool products sold in the shop. It extends `Product` and adds extra product details such as dimensions.

### `ShoppingCart`

`ShoppingCart` manages the customer’s selected items using a custom linked list instead of Java’s built-in list classes.

### `CartItem`

`CartItem` represents one product inside the cart together with the quantity selected by the customer.

### `Order`

`Order` represents a completed customer purchase.

### `OrderProcessor`

`OrderProcessor` handles checkout by validating stock, updating the database, creating the order, saving order items, and storing the order file.

### `OrderFileStorage`

`OrderFileStorage` saves a text-file copy of completed orders inside the `orders` folder.

### `ProductDAO`, `UserDAO`, and Order DAO Classes

These classes handle database operations for products, users, orders, and discounts.

---

## Discount System

ToolProShop supports discounts through records stored in the `Discount_Settings` database table.

Each discount setting includes:

- Discount label
- Discount percentage
- Minimum order value
- Minimum item count
- Enabled or disabled status

During shopping and checkout, the system checks which discounts are currently enabled and whether the customer’s cart meets the required conditions.

This makes the discount system flexible because the administrator can manage discounts without changing the main checkout code.

The current implementation does not use a separate `com.shopping.discount` package. Instead, discount settings are managed through the database and DAO layer.

---

## Default Login Accounts

The application creates default users when it starts.

### Admin Account

```text
Username: admin
Password: admin123
```

### Customer Account

```text
Username: shopper
Password: shop123
```

---

## How to Run

### 1. Clone the Repository

```bash
git clone https://github.com/Senatlo/E-Commerce-In-Java-ToolProShop.git
cd E-Commerce-In-Java-ToolProShop
```

### 2. Compile the Project

On Windows:

```bash
javac -cp ".;h2.jar" -d out src/com/shopping/Main.java src/com/shopping/**/*.java
```

On macOS or Linux:

```bash
javac -cp ".:h2.jar" -d out src/com/shopping/Main.java src/com/shopping/**/*.java
```

### 3. Run the Application

On Windows:

```bash
java -cp "out;h2.jar" com.shopping.Main
```

On macOS or Linux:

```bash
java -cp "out:h2.jar" com.shopping.Main
```

---

## Database

The project uses an H2 database. The database is created automatically when the application starts.

Main database tables include:

- `Users`
- `Products`
- `Orders`
- `Order_Items`
- `Discount_Settings`

---

## Application Workflow

1. The user starts the application.
2. The login screen appears.
3. Customers can browse products and shop.
4. Admins can manage products, users, orders, and discounts.
5. When checkout is completed, the system:
   - Validates stock
   - Updates product quantities
   - Saves the order in the database
   - Saves a text copy of the order
   - Shows a receipt

---

## Learning Purpose

This project was built to show how Object-Oriented Programming can be used in a real-world style application. It combines GUI development, database access, file handling, custom data structures, exception handling, and core OOP principles in one complete Java project.

---

## License

This project is licensed under the Apache-2.0 License.