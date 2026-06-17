const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const crypto = require('crypto');
const fs = require('fs');

// Load environment variables if a .env file exists (useful for local development)
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// SECURITY FIX (VULN-11): Restrict CORS to known origins only.
// Never use cors() with no configuration — it allows ALL origins (wildcard).
const allowedOrigins = [
    // Add your trusted admin web panel origin here when you build one.
    // Example: 'https://admin.mayankgents.com'
    // For now, only server-to-server calls (no Origin header) are allowed.
];
const corsOptions = {
    origin: (origin, callback) => {
        // Allow requests with no origin (server-to-server, Postman) and known origins only.
        if (!origin || allowedOrigins.includes(origin)) {
            callback(null, true);
        } else {
            callback(new Error(`CORS blocked for origin: ${origin}`));
        }
    },
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization'],
};
app.use(cors(corsOptions));

const path = require('path');

// SECURITY FIX (VULN-17): Parse JSON with a size limit to prevent payload DoS attacks.
app.use(express.json({ limit: '10kb' }));

// Serve static download assets
app.use('/download', express.static(path.join(__dirname, 'public')));

// SECURITY FIX (VULN-17): Remove Express fingerprint header.
app.disable('x-powered-by');

// --- Database Configuration (MongoDB Only) ---
let isMongoConnected = false;

// Default initial seed data
const defaultServices = [
    { id: 1, name: "Sculpted Cut", price: 120.0, description: "Elite cut & visual architecture consultation", durationMin: 45, nameHindi: "", suitability: "", isPremium: false },
    { id: 2, name: "Artisan Color", price: 250.0, description: "Custom balayage coloring & gloss therapy", durationMin: 120, nameHindi: "", suitability: "", isPremium: false },
    { id: 3, name: "Deep Hydration", price: 85.0, description: "Intense botanical scalp organic bath", durationMin: 30, nameHindi: "", suitability: "", isPremium: false },
    { id: 4, name: "Signature Blowout", price: 75.0, description: "Silk infusion treatment extra volume blowout", durationMin: 60, nameHindi: "", suitability: "", isPremium: false },
    // Premium Hair Sculptures
    { id: 5, name: "Royal Taper Fade", price: 350.0, description: "Precision-engineered classic finish, seamless side blend.", durationMin: 45, nameHindi: "शाही टेपर फेड", suitability: "Round & Oval Faces • Soft hair", isPremium: true },
    { id: 6, name: "Textured Feather Crop", price: 450.0, description: "Organic layered volume with sharp fluid crown movement.", durationMin: 45, nameHindi: "लेयर्ड फेदर क्रॉप", suitability: "Square & Heart Faces • Thick hair", isPremium: true },
    { id: 7, name: "Executive Pompadour", price: 300.0, description: "High royal volume front sweep with meticulous temple shape.", durationMin: 45, nameHindi: "द एक्सीक्यूटिव पॉम्पाडोर", suitability: "All Face Types • Voluble hair", isPremium: true },
    { id: 8, name: "Velvet Bob Contour", price: 400.0, description: "Ultra-sleek French bob lines with custom side profile shaping.", durationMin: 45, nameHindi: "मखमली बॉब", suitability: "Oval & Diamond Faces • Straight hair", isPremium: true }
];

const defaultStylists = [
    { id: 1, name: "Mayank Sharma", specialty: "Owner & Master Stylist", isAvailable: true, avatarColorIndex: 0, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 2, name: "Priya Iyer", specialty: "Lead Hair Artisan", isAvailable: true, avatarColorIndex: 1, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 3, name: "Amit Patel", specialty: "Creative Director", isAvailable: false, avatarColorIndex: 2, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 4, name: "Rohan Das", specialty: "Treatments Lead", isAvailable: true, avatarColorIndex: 3, imageUrl: null, awayUntilDate: null, awayUntilTime: null }
];

// JSON File Database Helper Functions (fallback for offline/local run)
const JSON_DB_PATH = path.join(__dirname, 'database.json');

function initJsonDb() {
    if (!fs.existsSync(JSON_DB_PATH)) {
        const initialData = {
            services: defaultServices,
            stylists: defaultStylists,
            bookings: []
        };
        fs.writeFileSync(JSON_DB_PATH, JSON.stringify(initialData, null, 2), 'utf8');
        console.log("Initialized new local JSON database.");
    }
}

function readJsonDb() {
    initJsonDb();
    try {
        const data = fs.readFileSync(JSON_DB_PATH, 'utf8');
        return JSON.parse(data);
    } catch (err) {
        console.error("Error reading JSON database, returning defaults", err);
        return { services: defaultServices, stylists: defaultStylists, bookings: [] };
    }
}

function writeJsonDb(data) {
    try {
        fs.writeFileSync(JSON_DB_PATH, JSON.stringify(data, null, 2), 'utf8');
    } catch (err) {
        console.error("Error writing JSON database", err);
    }
}

// MongoDB Schemas & Models
const serviceSchema = new mongoose.Schema({
    id: { type: Number, required: true, unique: true },
    name: { type: String, required: true },
    price: { type: Number, required: true },
    description: { type: String, default: "" },
    durationMin: { type: Number, default: 30 },
    nameHindi: { type: String, default: "" },
    suitability: { type: String, default: "" },
    isPremium: { type: Boolean, default: false }
});

const stylistSchema = new mongoose.Schema({
    id: { type: Number, required: true, unique: true },
    name: { type: String, required: true },
    specialty: { type: String, required: true },
    isAvailable: { type: Boolean, default: true },
    avatarColorIndex: { type: Number, default: 0 },
    imageUrl: { type: String, default: null },
    awayUntilDate: { type: String, default: null },
    awayUntilTime: { type: String, default: null }
});

const bookingSchema = new mongoose.Schema({
    id: { type: Number, required: true, unique: true },
    phoneNumber: { type: String, default: "" },
    clientName: { type: String, default: "" },
    services: { type: String, default: "" },
    stylistName: { type: String, default: "" },
    date: { type: String, default: "" },
    timeSlot: { type: String, default: "" },
    status: { type: String, default: "Active" },
    priceEstimate: { type: Number, default: 0.0 },
    timestamp: { type: Number, default: Date.now }
});

const Service = mongoose.model('Service', serviceSchema);
const Stylist = mongoose.model('Stylist', stylistSchema);
const Booking = mongoose.model('Booking', bookingSchema);

// MongoDB Database Seed
async function seedMongoIfEmpty() {
    try {
        for (const service of defaultServices) {
            const exists = await Service.findOne({ id: service.id });
            if (!exists) {
                await Service.create(service);
                console.log(`Seeded default service "${service.name}" to MongoDB.`);
            }
        }
        for (const stylist of defaultStylists) {
            const exists = await Stylist.findOne({ id: stylist.id });
            if (!exists) {
                await Stylist.create(stylist);
                console.log(`Seeded default stylist "${stylist.name}" to MongoDB.`);
            }
        }
    } catch (err) {
        console.error("Error seeding MongoDB database:", err);
    }
}

// Database Connection Orchestrator
async function connectDatabase() {
    const mongoUri = process.env.MONGODB_URI;
    if (mongoUri) {
        try {
            console.log("Attempting to connect to MongoDB...");
            await mongoose.connect(mongoUri, {
                serverSelectionTimeoutMS: 5000
            });
            console.log("Connected to MongoDB successfully!");
            isMongoConnected = true;
            await seedMongoIfEmpty();
        } catch (err) {
            console.error("MongoDB connection failed!", err.message);
            isMongoConnected = false;
        }
    } else {
        console.error("MONGODB_URI not set.");
        isMongoConnected = false;
    }
}

// Middleware to check database connection (logs warning but allows JSON database fallback)
const checkDbConnection = (req, res, next) => {
    if (!isMongoConnected) {
        console.warn("[DATABASE WARNING] MongoDB not connected, using database.json fallback.");
    }
    next();
};

// --- Server Routes Log Middleware ---
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

// Root URL Health Check (Doesn't block so we can see status info)
app.get('/', (req, res) => {
    res.json({
        status: isMongoConnected ? "Active" : "Error",
        message: isMongoConnected ? "mayank gents REST API Server is up and running!" : "Database Connection Failed!",
        databaseMode: isMongoConnected ? "MongoDB Atlas (Cloud)" : "None (Connection Failed)",
        version: "1.0.0"
    });
});

// App version update checker endpoint
app.get('/api/app-version', (req, res) => {
    const host = req.get('host');
    const protocol = req.headers['x-forwarded-proto'] || req.protocol;
    const downloadUrl = `${protocol}://${host}/download/mayank-gents.apk`;
    res.json({
        versionCode: 2,
        versionName: "1.0.1",
        downloadUrl: downloadUrl,
        updateMessage: "Number-wise bookings on admin dashboard and automatic past booking filtering.",
        forceUpdate: false
    });
});

// Apply database connection check on all api endpoints
app.use('/api', checkDbConnection);

// --- SERVICES ENDPOINTS ---
app.get('/api/services', async (req, res) => {
    if (isMongoConnected) {
        try {
            const list = await Service.find().sort({ id: 1 });
            res.json(list);
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        res.json(db.services);
    }
});

app.post('/api/services', async (req, res) => {
    const { name, price, description, durationMin, nameHindi, suitability, isPremium } = req.body;
    if (!name || isNaN(price)) {
        return res.status(400).json({ error: "Missing name or valid price" });
    }

    if (isMongoConnected) {
        try {
            const lastService = await Service.findOne().sort({ id: -1 });
            const nextId = lastService ? lastService.id + 1 : 1;
            const newService = new Service({
                id: nextId,
                name,
                price: parseFloat(price),
                description: description || "",
                durationMin: parseInt(durationMin) || 30,
                nameHindi: nameHindi || "",
                suitability: suitability || "",
                isPremium: isPremium === true || isPremium === "true"
            });
            await newService.save();
            res.status(201).json(newService);
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        const maxId = db.services.length > 0 ? Math.max(...db.services.map(s => s.id)) : 0;
        const newService = {
            id: maxId + 1,
            name,
            price: parseFloat(price),
            description: description || "",
            durationMin: parseInt(durationMin) || 30,
            nameHindi: nameHindi || "",
            suitability: suitability || "",
            isPremium: isPremium === true || isPremium === "true"
        };
        db.services.push(newService);
        writeJsonDb(db);
        res.status(201).json(newService);
    }
});

app.delete('/api/services/:id', async (req, res) => {
    const id = parseInt(req.params.id);
    if (isNaN(id)) {
        return res.status(400).json({ error: "Invalid ID" });
    }

    if (isMongoConnected) {
        try {
            await Service.deleteOne({ id });
            res.status(204).send();
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        db.services = db.services.filter(s => s.id !== id);
        writeJsonDb(db);
        res.status(204).send();
    }
});

// --- STYLISTS ENDPOINTS ---
app.get('/api/stylists', async (req, res) => {
    if (isMongoConnected) {
        try {
            const list = await Stylist.find().sort({ id: 1 });
            res.json(list);
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        res.json(db.stylists);
    }
});

app.post('/api/stylists', async (req, res) => {
    const { name, specialty, isAvailable, avatarColorIndex, imageUrl, awayUntilDate, awayUntilTime } = req.body;
    if (!name || !specialty) {
        return res.status(400).json({ error: "Missing name or specialty" });
    }

    if (isMongoConnected) {
        try {
            const lastStylist = await Stylist.findOne().sort({ id: -1 });
            const nextId = lastStylist ? lastStylist.id + 1 : 1;
            const newStylist = new Stylist({
                id: nextId,
                name,
                specialty,
                isAvailable: isAvailable ?? true,
                avatarColorIndex: parseInt(avatarColorIndex) || 0,
                imageUrl: imageUrl || null,
                awayUntilDate: awayUntilDate || null,
                awayUntilTime: awayUntilTime || null
            });
            await newStylist.save();
            res.status(201).json(newStylist);
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        const maxId = db.stylists.length > 0 ? Math.max(...db.stylists.map(s => s.id)) : 0;
        const newStylist = {
            id: maxId + 1,
            name,
            specialty,
            isAvailable: isAvailable ?? true,
            avatarColorIndex: parseInt(avatarColorIndex) || 0,
            imageUrl: imageUrl || null,
            awayUntilDate: awayUntilDate || null,
            awayUntilTime: awayUntilTime || null
        };
        db.stylists.push(newStylist);
        writeJsonDb(db);
        res.status(201).json(newStylist);
    }
});

app.delete('/api/stylists/:id', async (req, res) => {
    const id = parseInt(req.params.id);
    if (isNaN(id)) {
        return res.status(400).json({ error: "Invalid ID" });
    }

    if (isMongoConnected) {
        try {
            await Stylist.deleteOne({ id });
            res.status(204).send();
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        db.stylists = db.stylists.filter(s => s.id !== id);
        writeJsonDb(db);
        res.status(204).send();
    }
});

// --- BOOKINGS ENDPOINTS ---
app.get('/api/bookings', async (req, res) => {
    if (isMongoConnected) {
        try {
            const list = await Booking.find().sort({ timestamp: -1 });
            res.json(list);
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        const list = [...db.bookings].sort((a, b) => b.timestamp - a.timestamp);
        res.json(list);
    }
});

app.post('/api/bookings', async (req, res) => {
    const { phoneNumber, clientName, servicesName, stylistName, date, timeSlot, status, priceEstimate, timestamp } = req.body;

    if (isMongoConnected) {
        try {
            // --- Duplicate booking conflict check ---
            if (stylistName && date && timeSlot) {
                const conflicting = await Booking.findOne({
                    stylistName: stylistName,
                    date: date,
                    timeSlot: timeSlot,
                    status: 'Active'
                });
                if (conflicting) {
                    return res.status(409).json({
                        error: 'Booking conflict',
                        message: `Time slot ${timeSlot} on ${date} is already booked for ${stylistName}.`
                    });
                }
            }

            const lastBooking = await Booking.findOne().sort({ id: -1 });
            const nextId = lastBooking ? lastBooking.id + 1 : 1;
            const newBooking = new Booking({
                id: nextId,
                phoneNumber: phoneNumber || "",
                clientName: clientName || "",
                services: servicesName || req.body.services || "",
                stylistName: stylistName || "",
                date: date || "",
                timeSlot: timeSlot || "",
                status: status || "Active",
                priceEstimate: parseFloat(priceEstimate) || 0.0,
                timestamp: timestamp || Date.now()
            });
            await newBooking.save();
            res.status(201).json(newBooking);
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        // --- Duplicate booking conflict check ---
        if (stylistName && date && timeSlot) {
            const conflicting = db.bookings.find(b => 
                b.stylistName === stylistName && 
                b.date === date && 
                b.timeSlot === timeSlot && 
                b.status === 'Active'
            );
            if (conflicting) {
                return res.status(409).json({
                    error: 'Booking conflict',
                    message: `Time slot ${timeSlot} on ${date} is already booked for ${stylistName}.`
                });
            }
        }

        const maxId = db.bookings.length > 0 ? Math.max(...db.bookings.map(b => b.id)) : 0;
        const newBooking = {
            id: maxId + 1,
            phoneNumber: phoneNumber || "",
            clientName: clientName || "",
            services: servicesName || req.body.services || "",
            stylistName: stylistName || "",
            date: date || "",
            timeSlot: timeSlot || "",
            status: status || "Active",
            priceEstimate: parseFloat(priceEstimate) || 0.0,
            timestamp: timestamp || Date.now()
        };
        db.bookings.push(newBooking);
        writeJsonDb(db);
        res.status(201).json(newBooking);
    }
});

app.delete('/api/bookings/:id', async (req, res) => {
    const id = parseInt(req.params.id);
    if (isNaN(id)) {
        return res.status(400).json({ error: "Invalid ID" });
    }

    if (isMongoConnected) {
        try {
            await Booking.deleteOne({ id });
            res.status(204).send();
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        db.bookings = db.bookings.filter(b => b.id !== id);
        writeJsonDb(db);
        res.status(204).send();
    }
});

app.put('/api/bookings/:id/status', async (req, res) => {
    const id = parseInt(req.params.id);
    if (isNaN(id)) {
        return res.status(400).json({ error: "Invalid ID" });
    }

    const { status } = req.body;
    if (!status) {
        return res.status(400).json({ error: "Missing status" });
    }

    if (isMongoConnected) {
        try {
            const updatedBooking = await Booking.findOneAndUpdate(
                { id },
                { status },
                { new: true }
            );
            if (!updatedBooking) {
                return res.status(404).json({ error: "Booking not found" });
            }
            res.json(updatedBooking);
        } catch (e) {
            res.status(500).json({ error: e.message });
        }
    } else {
        const db = readJsonDb();
        const bookingIdx = db.bookings.findIndex(b => b.id === id);
        if (bookingIdx === -1) {
            return res.status(404).json({ error: "Booking not found" });
        }
        db.bookings[bookingIdx].status = status;
        writeJsonDb(db);
        res.json(db.bookings[bookingIdx]);
    }
});

// SECURITY FIX (VULN-08): Simple in-memory rate limiter for login endpoint.
// For production use the 'express-rate-limit' npm package instead.
const loginAttempts = new Map();
const MAX_LOGIN_ATTEMPTS = 5;
const LOCKOUT_WINDOW_MS = 15 * 60 * 1000; // 15 minutes

app.post('/api/admin/login', (req, res) => {
    const { email, password } = req.body;

    // SECURITY FIX (VULN-08): Rate limiting — track failed attempts by IP
    const clientIp = req.ip || req.connection.remoteAddress;
    const attemptData = loginAttempts.get(clientIp) || { count: 0, firstAttempt: Date.now() };
    const windowElapsed = Date.now() - attemptData.firstAttempt;

    if (windowElapsed > LOCKOUT_WINDOW_MS) {
        // Reset window
        attemptData.count = 0;
        attemptData.firstAttempt = Date.now();
    }

    if (attemptData.count >= MAX_LOGIN_ATTEMPTS) {
        const retryAfterSec = Math.ceil((LOCKOUT_WINDOW_MS - windowElapsed) / 1000);
        return res.status(429).json({
            error: 'Too many login attempts. Try again later.',
            retryAfterSeconds: retryAfterSec
        });
    }

    // SECURITY FIX (VULN-01): NEVER use || fallback for secrets.
    // If ADMIN_EMAIL or ADMIN_PASSWORD env vars are not set, refuse all logins.
    const adminEmail = process.env.ADMIN_EMAIL;
    const adminPassword = process.env.ADMIN_PASSWORD;

    if (!adminEmail || !adminPassword) {
        console.error('[SECURITY] ADMIN_EMAIL or ADMIN_PASSWORD environment variables are not set!');
        return res.status(500).json({ error: 'Server authentication is not configured.' });
    }

    // SECURITY FIX (VULN-01): Use constant-time comparison to prevent timing attacks.
    let emailMatch = false;
    let passMatch = false;
    try {
        const emailBuf = Buffer.from(email || '', 'utf8');
        const adminEmailBuf = Buffer.from(adminEmail, 'utf8');
        // timingSafeEqual requires same-length buffers; different lengths = no match
        if (emailBuf.length === adminEmailBuf.length) {
            emailMatch = crypto.timingSafeEqual(emailBuf, adminEmailBuf);
        }

        const passBuf = Buffer.from(password || '', 'utf8');
        const adminPassBuf = Buffer.from(adminPassword, 'utf8');
        if (passBuf.length === adminPassBuf.length) {
            passMatch = crypto.timingSafeEqual(passBuf, adminPassBuf);
        }
    } catch (e) {
        emailMatch = false;
        passMatch = false;
    }

    if (emailMatch && passMatch) {
        // Reset attempt counter on successful login
        loginAttempts.delete(clientIp);
        res.json({
            success: true,
            email: adminEmail,
            displayName: 'Executive Admin',
            customGreeting: 'Welcome back, Executive Admin'
        });
    } else {
        // Increment failed attempt counter
        attemptData.count += 1;
        loginAttempts.set(clientIp, attemptData);
        res.status(401).json({
            success: false,
            error: 'Invalid credentials'
        });
    }
});


// SECURITY FIX (VULN-02): The admin database wipe endpoint is DISABLED in production.
// This endpoint previously allowed ANYONE on the internet to wipe the entire MongoDB
// database with a single unauthenticated DELETE request.
// It is only available in non-production environments and requires authentication.
if (process.env.NODE_ENV !== 'production') {
    app.delete('/api/all-tables', async (req, res) => {
        // Additional safety: require a secret header even in dev
        const devSecret = req.headers['x-dev-secret'];
        if (!devSecret || devSecret !== process.env.DEV_SECRET) {
            return res.status(401).json({ error: 'Unauthorized: missing x-dev-secret header' });
        }
        if (isMongoConnected) {
            try {
                await Service.deleteMany({});
                await Stylist.deleteMany({});
                await Booking.deleteMany({});
                res.status(204).send();
            } catch (e) {
                res.status(500).json({ error: 'Failed to clear tables' });
            }
        } else {
            const db = readJsonDb();
            db.services = [];
            db.stylists = [];
            db.bookings = [];
            writeJsonDb(db);
            res.status(204).send();
        }
    });
}

// Start database connection first, then start server
connectDatabase().then(() => {
    app.listen(PORT, '0.0.0.0', () => {
        console.log(`mayank gents server listening on port ${PORT}`);
    });
});
