/**
 * Rule-based AI Explanation Service
 * Maps medical terms to plain-English explanations at the response level.
 * Does NOT modify any database values.
 */

// Diagnosis dictionary
const DIAGNOSIS_DICT = {
    "Acute Pulpitis": "Severe inflammation of the nerve inside the tooth, usually causing sharp pain",
    "Chronic Pulpitis": "Long-term nerve inflammation in the tooth, often with dull, lingering pain",
    "Periodontitis": "Serious infection of the gums and bone that support the teeth",
    "Gingivitis": "Early-stage gum disease causing swollen and bleeding gums",
    "Chronic Gingivitis": "Ongoing inflammation of the gums, often due to plaque buildup",
    "Dental Abscess": "A pocket of pus caused by a bacterial infection in or around a tooth",
    "Acute Dental Abscess": "A sudden, severe tooth infection forming a painful pus pocket",
    "Pericoronitis": "Inflammation around a partially erupted tooth, often a wisdom tooth",
    "Tooth Infection": "A bacterial infection that spreads to the tooth root or surrounding bone",
    "Pulpitis": "Inflammation of the dental pulp (nerve tissue) inside the tooth",
    "Dental Caries": "Tooth decay caused by acid-producing bacteria breaking down enamel",
    "Oral Candidiasis": "A fungal infection (thrush) inside the mouth",
};

// Drug name dictionary (maps exact drug_name values from drug_master)
const DRUG_DICT = {
    "Amoxicillin": "Antibiotic that fights bacterial tooth infections",
    "Ibuprofen": "Anti-inflammatory pain reliever that reduces swelling and discomfort",
    "Paracetamol": "Pain reliever and fever reducer, easy on the stomach",
    "Metronidazole": "Antibiotic that targets anaerobic bacteria found in gum disease",
    "Clindamycin": "Antibiotic used when other antibiotics cause allergies",
    "Azithromycin": "Broad-spectrum antibiotic often used for infections in allergy cases",
    "Diclofenac": "Anti-inflammatory used to reduce severe dental pain and swelling",
    "Doxycycline": "Antibiotic that also helps control gum inflammation",
    "Cephalexin": "First-generation antibiotic used for tooth and gum infections",
    "Chlorhexidine Mouthwash": "Antiseptic mouthwash that reduces bacteria and prevents gum disease",
};

/**
 * Returns a plain-English explanation for a given drug name.
 * Falls back to a generic label if drug is not in the dictionary.
 */
const explainDrug = (drugName) => {
    return DRUG_DICT[drugName] || "Medication prescribed by your doctor";
};

/**
 * Returns a plain-English explanation for a given diagnosis.
 * Falls back to the original text if not in the dictionary.
 */
const explainDiagnosis = (diagnosis) => {
    if (!diagnosis) return "No diagnosis recorded";
    // Normalize and attempt direct lookup, then partial match
    const direct = DIAGNOSIS_DICT[diagnosis];
    if (direct) return direct;

    const lower = diagnosis.toLowerCase();
    for (const [key, value] of Object.entries(DIAGNOSIS_DICT)) {
        if (lower.includes(key.toLowerCase())) return value;
    }
    return `Condition: ${diagnosis}`;
};

/**
 * Builds the full simplified explanation object for a prescription.
 * @param {Object} prescription - The prescription row from DB
 * @param {Array}  drugs        - The array of prescription_drugs rows (with drug_name joined)
 * @returns {Object}
 */
exports.buildExplanation = (prescription, drugs) => {
    const simplifiedDrugs = drugs.map((item) => ({
        drug_name: item.drug_name,
        quantity: item.quantity,
        frequency: item.frequency,
        what_it_does: explainDrug(item.drug_name),
    }));

    return {
        original: {
            id: prescription.id,
            patient_id: prescription.patient_id,
            doctor_id: prescription.doctor_id,
            diagnosis: prescription.diagnosis,
            remarks: prescription.remarks,
            status: prescription.status,
            created_at: prescription.created_at,
        },
        simplified_explanation: {
            diagnosis: explainDiagnosis(prescription.diagnosis),
            remarks: prescription.remarks || "No additional instructions",
            drugs: simplifiedDrugs,
        },
    };
};
