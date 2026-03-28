require("dotenv").config();
const mysql = require("mysql2");

const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: process.env.DB_PORT || 3306
});

const drugs = [
    "Amoxicillin 500mg",
    "Amoxyclav (Augmentin) 625mg",
    "Azithromycin 500mg",
    "Cephalexin (Keflex) 500mg",
    "Clindamycin 300mg",
    "Metronidazole 400mg",
    "Doxycycline 100mg",
    "Ciprofloxacin 500mg",
    "Erythromycin 500mg",
    "Paracetamol 500mg",
    "Ibuprofen 400mg",
    "Diclofenac 50mg",
    "Aceclofenac 100mg",
    "Naproxen 250mg",
    "Tramadol 50mg",
    "Fluconazole 150mg",
    "Nystatin 100,000 IU",
    "Clotrimazole 10mg",
    "Aciclovir 400mg",
    "Valaciclovir 1g",
    "Betamethasone (Topical)",
    "Triamcinolone acetonide 0.1%",
    "Prednisolone 20mg",
    "Hydrocortisone (Topical)",
    "Diazepam 2mg",
    "Lorazepam 1mg",
    "Midazolam (oral) 0.25mg/kg",
    "Pilocarpine 5mg",
    "Glycopyrrolate 1mg",
    "Chlorhexidine gluconate 0.2%",
    "Hyaluronic acid gel (Topical)",
    "Lidocaine ointment 2-5%",
    "Cetirizine 10mg",
    "Cyproheptadine 4mg",
    "Aspirin 300mg"
];

async function seed() {
    console.log("Starting drug seeding...");
    let addedCount = 0;
    let skippedCount = 0;

    for (const drugName of drugs) {
        try {
            const [exists] = await db.promise().query("SELECT id FROM drug_master WHERE name = ?", [drugName]);
            if (exists.length > 0) {
                console.log(`Skipping (Already exists): ${drugName}`);
                skippedCount++;
            } else {
                await db.promise().query("INSERT INTO drug_master (name, is_active) VALUES (?, TRUE)", [drugName]);
                console.log(`Added: ${drugName}`);
                addedCount++;
            }
        } catch (err) {
            console.error(`Error adding ${drugName}:`, err.message);
        }
    }

    console.log("------------------------");
    console.log(`Seeding complete! Added: ${addedCount}, Skipped: ${skippedCount}`);
    db.end();
}

seed();
