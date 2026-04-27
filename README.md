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

This project demonstrates several important OOP concepts:

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