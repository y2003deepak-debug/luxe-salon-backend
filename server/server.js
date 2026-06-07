const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const mongoose = require('mongoose');

// Load environment variables if a .env file exists (useful for local development)
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS so the Android app and web views can interact with the server safely
app.use(cors());
app.use(express.json());

// --- Database Configuration (MongoDB & JSON Fallback) ---
let isMongoConnected = false;
const JSON_DB_PATH = path.join(__dirname, 'database.json');

// Default initial seed data
const defaultServices = [
    { id: 1, name: "Sculpted Cut", price: 120.0, description: "Elite cut & visual architecture consultation", durationMin: 45 },
    { id: 2, name: "Artisan Color", price: 250.0, description: "Custom balayage coloring & gloss therapy", durationMin: 120 },
    { id: 3, name: "Deep Hydration", price: 85.0, description: "Intense botanical scalp organic bath", durationMin: 30 },
    { id: 4, name: "Signature Blowout", price: 75.0, description: "Silk infusion treatment extra volume blowout", durationMin: 60 }
];

const defaultStylists = [
    { id: 1, name: "Aarav Sharma", specialty: "Master Colorist", isAvailable: true, avatarColorIndex: 0, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 2, name: "Priya Iyer", specialty: "Lead Hair Artisan", isAvailable: true, avatarColorIndex: 1, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 3, name: "Amit Patel", specialty: "Creative Director", isAvailable: false, avatarColorIndex: 2, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 4, name: "Rohan Das", specialty: "Treatments Lead", isAvailable: true, avatarColorIndex: 3, imageUrl: null, awayUntilDate: null, awayUntilTime: null }
];

// MongoDB Schemas & Models
const serviceSchema = new mongoose.Schema({
    id: { type: Number, required: true, unique: true },
    name: { type: String, required: true },
    price: { type: Number, required: true },
    description: { type: String, default: "" },
    durationMin: { type: Number, default: 30 }
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

// JSON File Database Helper Functions
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
        console.error("Error writing to JSON database", err);
    }
}

// MongoDB Database Seed
async function seedMongoIfEmpty() {
    try {
        const serviceCount = await Service.countDocuments();
        if (serviceCount === 0) {
            await Service.insertMany(defaultServices);
            console.log("Seeded default services to MongoDB.");
        }
        const stylistCount = await Stylist.countDocuments();
        if (stylistCount === 0) {
            await Stylist.insertMany(defaultStylists);
            console.log("Seeded default stylists to MongoDB.");
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
            console.error("MongoDB connection failed! Falling back to local JSON database.", err.message);
            isMongoConnected = false;
        }
    } else {
        console.log("MONGODB_URI not set. Running with local JSON database fallback.");
        isMongoConnected = false;
    }

    if (!isMongoConnected) {
        initJsonDb();
    }
}

// --- Server Routes Log Middleware ---
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

// Root URL Health Check
app.get('/', (req, res) => {
    res.json({
        status: "Active",
        message: "Luxe Salon REST API Server is up and running!",
        databaseMode: isMongoConnected ? "MongoDB Atlas (Cloud)" : "JSON File (Local Fallback)",
        version: "1.0.0"
    });
});

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
    const { name, price, description, durationMin } = req.body;
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
                durationMin: parseInt(durationMin) || 30
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
            durationMin: parseInt(durationMin) || 30
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
        res.json(db.bookings);
    }
});

app.post('/api/bookings', async (req, res) => {
    const { phoneNumber, clientName, servicesName, stylistName, date, timeSlot, status, priceEstimate, timestamp } = req.body;

    if (isMongoConnected) {
        try {
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

// Admin global table wipe option for testing
app.delete('/api/all-tables', async (req, res) => {
    if (isMongoConnected) {
        try {
            await Service.deleteMany({});
            await Stylist.deleteMany({});
            await Booking.deleteMany({});
            res.status(204).send();
        } catch (e) {
            res.status(500).json({ error: e.message });
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

// Start database connection first, then start server
connectDatabase().then(() => {
    app.listen(PORT, '0.0.0.0', () => {
        console.log(`Luxe Salon server listening on port ${PORT}`);
    });
});
