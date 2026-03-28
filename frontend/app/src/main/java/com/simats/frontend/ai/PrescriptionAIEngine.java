package com.simats.frontend.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DIAS Rx AI Prescription Explanation Engine
 * Implements rule-based NLP and semantic mapping to simplify medical prescriptions.
 * Suitable for undergraduate academic implementation - no external AI API required.
 */
public class PrescriptionAIEngine {

    // ─── Frequency Mappings ─────────────────────────────────────────────────────
    private static final Map<String, FrequencyInfo> FREQUENCY_MAP = new HashMap<>();

    // ─── Medical Abbreviation Mappings ──────────────────────────────────────────
    private static final Map<String, String> ABBREV_MAP = new HashMap<>();

    // ─── Drug Category Mappings ──────────────────────────────────────────────────
    private static final Map<String, String> DRUG_PURPOSE_MAP = new HashMap<>();

    static {
        // Frequencies
        FREQUENCY_MAP.put("OD",  new FrequencyInfo("Once Daily", "Take 1 dose every day", 1, new int[]{8}));
        FREQUENCY_MAP.put("BD",  new FrequencyInfo("Twice Daily", "Take 1 dose in the morning and 1 dose in the evening", 2, new int[]{8, 20}));
        FREQUENCY_MAP.put("TDS", new FrequencyInfo("3 Times Daily", "Take 1 dose in the morning, afternoon, and night", 3, new int[]{8, 14, 20}));
        FREQUENCY_MAP.put("QID", new FrequencyInfo("4 Times Daily", "Take 1 dose every 6 hours", 4, new int[]{6, 12, 18, 24}));
        FREQUENCY_MAP.put("PRN", new FrequencyInfo("As Needed", "Take only when you feel pain or as advised", 0, null));
        FREQUENCY_MAP.put("HS",  new FrequencyInfo("At Bedtime", "Take 1 dose at bedtime only", 1, new int[]{22}));
        FREQUENCY_MAP.put("SOS", new FrequencyInfo("If Required", "Take only when required, as advised by doctor", 0, null));
        FREQUENCY_MAP.put("STAT",new FrequencyInfo("Immediately", "Take this dose right away", 1, null));

        // Abbreviations
        ABBREV_MAP.put("TAB", "Tablet");
        ABBREV_MAP.put("CAP", "Capsule");
        ABBREV_MAP.put("SYR", "Syrup");
        ABBREV_MAP.put("INJ", "Injection");
        ABBREV_MAP.put("GEL", "Gel");
        ABBREV_MAP.put("CR",  "Cream");
        ABBREV_MAP.put("SUSP","Suspension");
        ABBREV_MAP.put("MDI", "Inhaler");
        ABBREV_MAP.put("RINSE","Mouth rinse");
        ABBREV_MAP.put("GARG","Gargle");
        ABBREV_MAP.put("MG",  "mg");
        ABBREV_MAP.put("ML",  "mL");
        ABBREV_MAP.put("AC",  "Before meals");
        ABBREV_MAP.put("PC",  "After meals");

        // Drug purposes (dental/general)
        DRUG_PURPOSE_MAP.put("amoxicillin",          "antibiotic to fight bacterial infection");
        DRUG_PURPOSE_MAP.put("metronidazole",         "antibiotic to treat gum infection");
        DRUG_PURPOSE_MAP.put("paracetamol",           "pain reliever and fever reducer");
        DRUG_PURPOSE_MAP.put("ibuprofen",             "anti-inflammatory pain reliever");
        DRUG_PURPOSE_MAP.put("diclofenac",            "pain and swelling reducer");
        DRUG_PURPOSE_MAP.put("chlorhexidine",         "antiseptic mouth rinse to reduce bacteria");
        DRUG_PURPOSE_MAP.put("triamcinolone",         "steroid paste to heal mouth ulcers");
        DRUG_PURPOSE_MAP.put("penicillin",            "antibiotic for bacterial infections");
        DRUG_PURPOSE_MAP.put("azithromycin",          "antibiotic for infections");
        DRUG_PURPOSE_MAP.put("cetirizine",            "antihistamine for allergy relief");
        DRUG_PURPOSE_MAP.put("omeprazole",            "to protect your stomach from acidity");
        DRUG_PURPOSE_MAP.put("pantoprazole",          "to reduce stomach acid");
        DRUG_PURPOSE_MAP.put("doxycycline",           "antibiotic for bacterial infections");
        DRUG_PURPOSE_MAP.put("clindamycin",           "antibiotic for serious dental infections");
        DRUG_PURPOSE_MAP.put("fluconazole",           "antifungal medicine");
        DRUG_PURPOSE_MAP.put("benzydamine",           "mouth spray/rinse for pain and swelling");
        DRUG_PURPOSE_MAP.put("nystatin",              "antifungal for oral thrush");
    }

    // ─── Simplified Prescription Object ─────────────────────────────────────────
    public static class SimplifiedDrug {
        public String originalName;
        public String purpose;
        public String strength;
        public String frequencyEnglish;
        public String frequencyTamil;
        public String timingEnglish;
        public String timingTamil;
        public String duration;
        public int[] reminderHours; // hours of day to remind (24h)

        public SimplifiedDrug(String originalName, String purpose, String strength,
                              String frequencyEnglish, String frequencyTamil,
                              String timingEnglish, String timingTamil,
                              String duration, int[] reminderHours) {
            this.originalName = originalName;
            this.purpose = purpose;
            this.strength = strength;
            this.frequencyEnglish = frequencyEnglish;
            this.frequencyTamil = frequencyTamil;
            this.timingEnglish = timingEnglish;
            this.timingTamil = timingTamil;
            this.duration = duration;
            this.reminderHours = reminderHours;
        }
    }

    // ─── Frequency Info ──────────────────────────────────────────────────────────
    public static class FrequencyInfo {
        public String english;
        public String timing;
        public int count;
        public int[] hours;

        public FrequencyInfo(String english, String timing, int count, int[] hours) {
            this.english = english;
            this.timing = timing;
            this.count = count;
            this.hours = hours;
        }
    }

    // ─── Tamil Frequency Map ─────────────────────────────────────────────────────
    private static final Map<String, String> TAMIL_FREQ = new HashMap<>();
    private static final Map<String, String> TAMIL_TIMING = new HashMap<>();

    static {
        TAMIL_FREQ.put("OD",  "ஒரு நாளைக்கு ஒரு முறை");
        TAMIL_FREQ.put("BD",  "ஒரு நாளைக்கு இரண்டு முறை");
        TAMIL_FREQ.put("TDS", "ஒரு நாளைக்கு மூன்று முறை");
        TAMIL_FREQ.put("QID", "ஒரு நாளைக்கு நான்கு முறை");
        TAMIL_FREQ.put("PRN", "தேவைப்படும்போது மட்டும்");
        TAMIL_FREQ.put("HS",  "தூங்குவதற்கு முன்பு");
        TAMIL_FREQ.put("SOS", "தேவைப்படும்போது எடுக்கவும்");

        TAMIL_TIMING.put("OD",  "காலை 8 மணிக்கு எடுக்கவும்");
        TAMIL_TIMING.put("BD",  "காலை 8 மணி மற்றும் இரவு 8 மணிக்கு எடுக்கவும்");
        TAMIL_TIMING.put("TDS", "காலை, மதியம், இரவு மூன்று வேளையும் எடுக்கவும்");
        TAMIL_TIMING.put("QID", "ஒவ்வொரு 6 மணி நேரத்திற்கும் ஒரு முறை எடுக்கவும்");
        TAMIL_TIMING.put("PRN", "வலி அல்லது தேவைப்படும்போது மட்டும் எடுக்கவும்");
        TAMIL_TIMING.put("HS",  "தூங்குவதற்கு முன்பு மட்டும் எடுக்கவும்");
    }

    // ─── Main Processing Method ──────────────────────────────────────────────────

    /**
     * Converts raw drug name + frequency + strength + duration into simplified patient instructions.
     */
    public static SimplifiedDrug explain(String drugName, String frequency, String strength, String duration) {
        String nameLower = drugName.toLowerCase(Locale.getDefault()).trim();

        // 1. Find purpose via semantic matching
        String purpose = "medicine prescribed by your doctor";
        for (Map.Entry<String, String> entry : DRUG_PURPOSE_MAP.entrySet()) {
            if (nameLower.contains(entry.getKey())) {
                purpose = entry.getValue();
                break;
            }
        }

        // 2. Extract frequency info
        String freqKey = frequency.toUpperCase(Locale.getDefault()).trim().split("\\s+")[0];
        FrequencyInfo freqInfo = FREQUENCY_MAP.get(freqKey);

        String engFreq = freqInfo != null ? freqInfo.english : frequency;
        String engTiming = freqInfo != null ? freqInfo.timing : "Take as directed by your doctor";
        String tamFreq = TAMIL_FREQ.containsKey(freqKey) ? TAMIL_FREQ.get(freqKey) : frequency;
        String tamTiming = TAMIL_TIMING.containsKey(freqKey) ? TAMIL_TIMING.get(freqKey) : "மருத்துவர் அறிவுறுத்தியபடி எடுக்கவும்";
        int[] hours = (freqInfo != null && freqInfo.hours != null) ? freqInfo.hours : new int[]{8};

        // 3. Build strength description
        String strengthDesc = strength != null && !strength.isEmpty() ? strength : "";

        return new SimplifiedDrug(
                drugName, purpose, strengthDesc,
                engFreq, tamFreq,
                engTiming, tamTiming,
                duration, hours
        );
    }

    /**
     * Generates a full voice script for a list of drugs (English).
     */
    public static String buildVoiceScriptEnglish(String patientName, List<SimplifiedDrug> drugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello ").append(patientName).append(". Here are your medication instructions. ");
        int i = 1;
        for (SimplifiedDrug d : drugs) {
            sb.append("Medicine ").append(i++).append(": ").append(d.originalName);
            if (!d.strength.isEmpty()) sb.append(", ").append(d.strength);
            sb.append(". This is an ").append(d.purpose).append(". ");
            sb.append(d.timingEnglish).append(". ");
            if (d.duration != null && !d.duration.isEmpty()) {
                sb.append("Continue for ").append(d.duration).append(". ");
            }
        }
        sb.append("Please complete the full course. Contact your doctor if you have any concerns.");
        return sb.toString();
    }

    /**
     * Generates a full voice script for a list of drugs (Tamil).
     */
    public static String buildVoiceScriptTamil(String patientName, List<SimplifiedDrug> drugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("வணக்கம் ").append(patientName).append(". உங்கள் மருந்துகள் பற்றிய தகவல்கள்: ");
        int i = 1;
        for (SimplifiedDrug d : drugs) {
            sb.append("மருந்து ").append(i++).append(": ").append(d.originalName).append(". ");
            sb.append(d.timingTamil).append(". ");
            if (d.duration != null && !d.duration.isEmpty()) {
                sb.append(d.duration).append(" தொடர்ந்து எடுக்கவும். ");
            }
        }
        sb.append("மருத்துவரின் ஆலோசனைப்படி மருந்துகளை முழுமையாக எடுக்கவும். நன்றி.");
        return sb.toString();
    }

    /**
     * AI Lifestyle Insights
     * Generates general wellness tips based on the category of drugs prescribed.
     */
    public static List<String> getLifestyleInsights(List<SimplifiedDrug> drugs) {
        List<String> insights = new java.util.ArrayList<>();
        boolean hasAntibiotic = false;
        boolean hasPainkiller = false;
        
        for (SimplifiedDrug d : drugs) {
            String p = d.purpose.toLowerCase();
            if (p.contains("infection") || p.contains("bacteria")) hasAntibiotic = true;
            if (p.contains("pain") || p.contains("inflammation")) hasPainkiller = true;
        }
        
        if (hasAntibiotic) {
            insights.add("Finish the entire course even if you feel better.");
            insights.add("Probiotics (like curd/yogurt) helps maintain gut health during treatment.");
        }
        if (hasPainkiller) {
            insights.add("Avoid taking pain medication on an empty stomach.");
            insights.add("Stay well-hydrated throughout the day.");
        }
        
        if (insights.isEmpty()) {
            insights.add("Always follow the exact timing mentioned in your prescription.");
            insights.add("Store your medications in a cool, dry place away from sunlight.");
        }
        
        return insights;
    }
}
