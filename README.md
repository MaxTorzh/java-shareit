# 🚀 ShareIt - Service for Sharing Things

![Java](https://img.shields.io/badge/Java-11%2B-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.9-brightgreen)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-orange)
![H2](https://img.shields.io/badge/Database-H2%20%2F%20PostgreSQL-lightgrey)

**ShareIt** — это микросервисное приложение для шеринга вещей. Пользователи могут брать в аренду нужные предметы и делиться своими, создавая сообщество доверия и взаимопомощи.

## 🌟 Особенности

- **🎯 Микросервисная архитектура** - разделение на gateway и server
- **📱 REST API** - полный набор эндпоинтов для клиентов
- **🔐 Валидация данных** - многоуровневая проверка входных данных
- **💾 Гибкая БД** - поддержка H2 (разработка) и PostgreSQL (продакшен)
- **🐳 Docker готовность** - контейнеризация приложения
- **📚 Документирование** - полная API документация

## 🏗️ Архитектура

### Микросервисная структура:
shareIt/
├── 📡 shareIt-gateway (порт 8080)
│ ├── Контроллеры с валидацией
│ ├── REST клиенты к server
│ └── Пользовательские DTO
│
└── 🖥️ shareIt-server (порт 9090)
├── Бизнес-логика и сервисы
├── Репозитории и модели БД
└── Внутренние DTO

### Стек технологий:
- **Backend**: Java 11, Spring Boot, Spring Data JPA
- **Database**: H2 (dev), PostgreSQL (prod)
- **Validation**: Bean Validation, Custom Validators
- **Build**: Maven
- **Testing**: JUnit, Mockito
- **Container**: Docker

## 📊 Основные сущности

| Сущность | Описание |
|----------|----------|
| **👤 User** | Пользователи системы |
| **🎯 Item** | Вещи для аренды |
| **📅 Booking** | Бронирования вещей |
| **💬 Comment** | Отзывы к арендам |
| **📋 ItemRequest** | Запросы на вещи |

📡 API Endpoints
Пользователи (/users)
POST /users - создание пользователя

PATCH /users/{id} - обновление пользователя

GET /users/{id} - получение пользователя

GET /users - список всех пользователей

DELETE /users/{id} - удаление пользователя

Вещи (/items)
POST /items - добавление вещи

PATCH /items/{id} - обновление вещи

GET /items/{id} - получение вещи

GET /items - вещи пользователя

GET /items/search - поиск вещей

Бронирования (/bookings)
POST /bookings - создание бронирования

PATCH /bookings/{id} - подтверждение/отклонение

GET /bookings/{id} - получение бронирования

GET /bookings - бронирования пользователя

