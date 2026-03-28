const PDFDocument = require("pdfkit");
const fs = require("fs");
const path = require("path");

const UPLOADS_DIR = path.join(__dirname, "..", "uploads", "prescriptions");

/**
 * Ensures the uploads directory exists at startup.
 */
const ensureDir = () => {
    if (!fs.existsSync(UPLOADS_DIR)) {
        fs.mkdirSync(UPLOADS_DIR, { recursive: true });
    }
};

/**
 * Generates a professional PDF prescription and saves it locally.
 * @param {Object} prescription - Prescription row from DB
 * @param {Array}  drugs        - Drug rows with drug_name joined
 * @param {Object} explanation  - Output of aiExplanationService.buildExplanation()
 * @returns {Promise<string>}   - Relative path to the saved file
 */
exports.generatePrescriptionPDF = (prescription, drugs, explanation) => {
    return new Promise((resolve, reject) => {
        ensureDir();

        const fileName = `prescription_${prescription.id}.pdf`;
        const filePath = path.join(UPLOADS_DIR, fileName);
        const relPath = `uploads/prescriptions/${fileName}`;

        const doc = new PDFDocument({ margin: 50, size: "A4" });
        const stream = fs.createWriteStream(filePath);

        doc.pipe(stream);

        /* ── COLOUR PALETTE ── */
        const NAVY = "#1a3c5e";
        const TEAL = "#0d7377";
        const LIGHT = "#f4f8fc";
        const GREY = "#666666";
        const BLACK = "#111111";

        /* ══════════════════════════════════════════════
           HEADER BAND
        ══════════════════════════════════════════════ */
        doc.rect(0, 0, doc.page.width, 90).fill(NAVY);

        doc
            .fillColor("#ffffff")
            .fontSize(22)
            .font("Helvetica-Bold")
            .text("DIAS Rx", 50, 25);

        doc
            .fontSize(10)
            .font("Helvetica")
            .text("Digital Prescription & Patient Communication System", 50, 52);

        doc
            .fontSize(9)
            .fillColor("#aaccee")
            .text(`Generated: ${new Date().toLocaleString("en-IN", { timeZone: "Asia/Kolkata" })}`, 50, 68);

        doc.y = 110;

        /* ══════════════════════════════════════════════
           PRESCRIPTION ID BADGE
        ══════════════════════════════════════════════ */
        doc
            .roundedRect(350, 100, 195, 38, 6)
            .fill(TEAL);
        doc
            .fillColor("#ffffff")
            .fontSize(10)
            .font("Helvetica-Bold")
            .text(`Rx #${prescription.id}`, 355, 110);
        doc
            .fontSize(8)
            .font("Helvetica")
            .text(`Status: ${prescription.status}`, 355, 124);

        /* ══════════════════════════════════════════════
           SECTION HELPER
        ══════════════════════════════════════════════ */
        const sectionHeader = (title, y) => {
            doc.rect(50, y, doc.page.width - 100, 22).fill(LIGHT);
            doc
                .fillColor(NAVY)
                .fontSize(11)
                .font("Helvetica-Bold")
                .text(title, 56, y + 5);
            return y + 30;
        };

        const field = (label, value, x, y, width) => {
            doc.fillColor(GREY).fontSize(8).font("Helvetica-Bold").text(label, x, y);
            const textVal = value || "—";
            doc.fillColor(BLACK).fontSize(9).font("Helvetica").text(textVal, x, y + 12, { width });

            // Return curY dynamically using heightOfString
            const textHeight = doc.heightOfString(textVal, { width });
            return y + 12 + textHeight + 15;
        };

        const addPageIfNeeded = (y, buffer = 50) => {
            if (y > doc.page.height - buffer - 50) { // Keep 50px for footer
                doc.addPage();
                return 50; // reset Y to top margin
            }
            return y;
        };

        /* ══════════════════════════════════════════════
           PATIENT & DOCTOR
        ══════════════════════════════════════════════ */
        let curY = 150;
        curY = sectionHeader("Patient & Doctor Information", curY);

        field("Patient ID", String(prescription.patient_id), 50, curY, 200);
        curY = field("Doctor ID", String(prescription.doctor_id), 300, curY, 200);

        field("Patient Name", prescription.patient_name || "—", 50, curY, 200);
        curY = field("Doctor Name", prescription.doctor_name || "—", 300, curY, 200);

        /* ══════════════════════════════════════════════
           DIAGNOSIS & REMARKS
        ══════════════════════════════════════════════ */
        curY = addPageIfNeeded(curY, 150);
        curY = sectionHeader("Diagnosis & Remarks", curY);
        curY = field("Diagnosis", prescription.diagnosis, 50, curY, 460);
        curY = field("Remarks", prescription.remarks || "None", 50, curY, 460);

        /* ══════════════════════════════════════════════
           DRUG TABLE
        ══════════════════════════════════════════════ */
        curY = addPageIfNeeded(curY, 150);
        curY = sectionHeader("Prescribed Medications", curY);

        // Column headers
        doc.fillColor(NAVY).fontSize(9).font("Helvetica-Bold");
        doc.text("#", 50, curY);
        doc.text("Drug Name", 75, curY);
        doc.text("Quantity", 270, curY);
        doc.text("Frequency", 345, curY);

        doc.moveTo(50, curY + 14).lineTo(555, curY + 14).strokeColor(NAVY).lineWidth(0.5).stroke();
        curY += 18;

        drugs.forEach((item, idx) => {
            curY = addPageIfNeeded(curY, 50);

            doc.fillColor(BLACK).font("Helvetica").fontSize(9);
            const drugName = item.drug_name;
            const freq = item.frequency || "—";

            const nameHeight = doc.heightOfString(drugName, { width: 180 });
            const freqHeight = doc.heightOfString(freq, { width: 160 });
            const rowHeight = Math.max(nameHeight, freqHeight) + 10;

            if ((idx + 1) % 2 === 0) {
                doc.rect(50, curY - 2, 505, rowHeight).fill("#f0f5fa").fillOpacity(0.5);
                doc.fillColor(BLACK);
            }

            doc.text(String(idx + 1), 50, curY);
            doc.text(drugName, 75, curY, { width: 180 });
            doc.text(String(item.quantity || "—"), 270, curY);
            doc.text(freq, 345, curY, { width: 160 });

            curY += rowHeight;
        });

        curY += 15;

        /* ══════════════════════════════════════════════
           AI SIMPLIFIED EXPLANATION
        ══════════════════════════════════════════════ */
        curY = addPageIfNeeded(curY, 150);
        curY = sectionHeader("Plain-Language Explanation (AI Simplified)", curY);

        doc
            .fillColor(GREY)
            .fontSize(8)
            .font("Helvetica-Oblique")
            .text("The following is a simplified explanation to help patients understand their prescription.", 50, curY, { width: 460 });

        curY += doc.heightOfString("The following...", { width: 460, font: "Helvetica-Oblique", fontSize: 8 }) + 15;

        curY = field("What does the diagnosis mean?", explanation.simplified_explanation.diagnosis, 50, curY, 460);

        explanation.simplified_explanation.drugs.forEach((drug) => {
            curY = addPageIfNeeded(curY, 60);

            doc.fillColor(TEAL).font("Helvetica-Bold").fontSize(9).text(`• ${drug.drug_name}`, 55, curY);

            const explanationText = drug.what_it_does;
            doc.fillColor(GREY).font("Helvetica").fontSize(8).text(explanationText, 55, curY + 12, { width: 440 });

            const textHeight = doc.heightOfString(explanationText, { width: 440, font: "Helvetica", fontSize: 8 });
            curY += 12 + textHeight + 15;
        });

        /* ══════════════════════════════════════════════
           FOOTER
        ══════════════════════════════════════════════ */
        const footerY = doc.page.height - 50;
        doc.moveTo(50, footerY - 10).lineTo(555, footerY - 10).strokeColor(NAVY).lineWidth(0.5).stroke();
        doc
            .fillColor(GREY)
            .fontSize(7)
            .font("Helvetica")
            .text(
                "This document is generated by DIAS Rx and is intended for the patient and treating physician only. Confidential.",
                50, footerY, { align: "center", width: 505 }
            );

        doc.end();

        stream.on("finish", () => resolve(relPath));
        stream.on("error", reject);
    });
};
