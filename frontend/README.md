# Frontend monorepo

This folder groups the two frontend apps into a single workspace:

- `kid/` — kid app (dev port 5173)
- `parent/` — parent app (dev port 5174)

Quick start:

1. From project root, install dependencies for each app (or use workspace install)

```bash
cd frontend/kid && npm install
cd ../parent && npm install
```

2. Start both apps (from project root)

```bash
npm --prefix frontend run dev
```

Notes:
- Both apps default to `VITE_API_BASE=http://localhost:8080`. You can override by exporting `VITE_API_BASE` before starting.
- Kid runs on port `5173`, Parent runs on `5174`.


