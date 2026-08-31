# CallMate AI — Cloud Authentication & User Management Backend

Privacy-first Node.js & Express REST API for CallMate AI account management and MongoDB Atlas integration.

---

## Architecture & Privacy Boundary

- **Cloud MongoDB Atlas**: Strictly contains user account metadata:
  - `userId`, `name`, `email`, `password` (bcrypt hash), `phoneNumber`, `accountStatus`, `createdAt`, `updatedAt`, `lastLogin`, `appVersion`.
- **Local Device (Offline)**:
  - Call transcripts, audio recordings, addresses, AI prompt instructions, and voice settings **never** upload to MongoDB.

---

## Setup & Running

### 1. Prerequisites
- Node.js >= 18
- MongoDB Atlas Free Tier account or local MongoDB instance

### 2. Configure Environment
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Edit `.env` with your MongoDB Atlas connection string:
```env
PORT=5000
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.mongodb.net/callmate_ai?retryWrites=true&w=majority
JWT_SECRET=your_super_secret_jwt_key
```

### 3. Install Dependencies
```bash
npm install
```

### 4. Start the Server
```bash
# Production mode
npm start

# Development mode with watch
npm run dev
```

The server listens on `http://0.0.0.0:5000`.

---

## REST Endpoints

### Public
- `GET /api/health` — System status & MongoDB connectivity
- `POST /api/auth/register` — Create new account (name, email, password, confirmPassword)
- `POST /api/auth/login` — Authenticate and receive JWT session token
- `POST /api/auth/logout` — End session

### Authenticated (`Authorization: Bearer <token>`)
- `GET /api/auth/me` — Retrieve active session user
- `GET /api/users/me` — Retrieve account metadata
- `PUT /api/users/me` — Update user name or phone number
- `DELETE /api/users/me` — Permanently delete user account from MongoDB
