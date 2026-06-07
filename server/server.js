const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS so the Android app and web views can interact with the server safely
app.use(cors());
app.use(express.json());

// --- Database Seed Data (In-Memory) ---
let services = [
    { id: 1, name: "Sculpted Cut", price: 120.0, description: "Elite cut & visual architecture consultation", durationMin: 45 },
    { id: 2, name: "Artisan Color", price: 250.0, description: "Custom balayage coloring & gloss therapy", durationMin: 120 },
    { id: 3, name: "Deep Hydration", price: 85.0, description: "Intense botanical scalp organic bath", durationMin: 30 },
    { id: 4, name: "Signature Blowout", price: 75.0, description: "Silk infusion treatment extra volume blowout", durationMin: 60 }
];

let stylists = [
    { id: 1, name: "Aarav Sharma", specialty: "Master Colorist", isAvailable: true, avatarColorIndex: 0, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 2, name: "Priya Iyer", specialty: "Lead Hair Artisan", isAvailable: true, avatarColorIndex: 1, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 3, name: "Amit Patel", specialty: "Creative Director", isAvailable: false, avatarColorIndex: 2, imageUrl: null, awayUntilDate: null, awayUntilTime: null },
    { id: 4, name: "Rohan Das", specialty: "Treatments Lead", isAvailable: true, avatarColorIndex: 3, imageUrl: null, awayUntilDate: null, awayUntilTime: null }
];

let bookings = [];

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
        version: "1.0.0"
    });
});

// --- SERVICES ENDPOINTS ---
app.get('/api/services', (req, res) => {
    res.json(services);
});

app.post('/api/services', (req, res) => {
    const { name, price, description, durationMin } = req.body;
    if (!name || isNaN(price)) {
        return res.status(400).json({ error: "Missing name or valid price" });
    }
    const maxId = services.length > 0 ? Math.max(...services.map(s => s.id)) : 0;
    const newService = {
        id: maxId + 1,
        name,
        price: parseFloat(price),
        description: description || "",
        durationMin: parseInt(durationMin) || 30
    };
    services.push(newService);
    res.status(201).json(newService);
});

app.delete('/api/services/:id', (req, res) => {
    const id = parseInt(req.params.id);
    services = services.filter(s => s.id !== id);
    res.status(204).send();
});

// --- STYLISTS ENDPOINTS ---
app.get('/api/stylists', (req, res) => {
    res.json(stylists);
});

app.post('/api/stylists', (req, res) => {
    const { name, specialty, isAvailable, avatarColorIndex, imageUrl, awayUntilDate, awayUntilTime } = req.body;
    if (!name || !specialty) {
        return res.status(400).json({ error: "Missing name or specialty" });
    }
    const maxId = stylists.length > 0 ? Math.max(...stylists.map(s => s.id)) : 0;
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
    stylists.push(newStylist);
    res.status(201).json(newStylist);
});

app.delete('/api/stylists/:id', (req, res) => {
    const id = parseInt(req.params.id);
    stylists = stylists.filter(s => s.id !== id);
    res.status(204).send();
});

// --- BOOKINGS ENDPOINTS ---
app.get('/api/bookings', (req, res) => {
    res.json(bookings);
});

app.post('/api/bookings', (req, res) => {
    const { phoneNumber, clientName, servicesName, stylistName, date, timeSlot, status, priceEstimate, timestamp } = req.body;
    const maxId = bookings.length > 0 ? Math.max(...bookings.map(b => b.id)) : 0;
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
    bookings.push(newBooking);
    res.status(201).json(newBooking);
});

// Admin global table wipe option for testing
app.delete('/api/all-tables', (req, res) => {
    services = [];
    stylists = [];
    bookings = [];
    res.status(204).send();
});

// Start the Express service
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Luxe Salon server listening on port ${PORT}`);
});
