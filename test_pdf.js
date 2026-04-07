const fs = require('fs');
const path = require('path');
const PDFDocument = require('pdfkit');

const testFile = path.join(__dirname, 'test_prescription.pdf');
console.log('Target file:', testFile);

try {
    const doc = new PDFDocument();
    const stream = fs.createWriteStream(testFile);
    doc.pipe(stream);
    doc.text('Testing generation from server');
    doc.end();

    stream.on('finish', () => {
        console.log('SUCCESS: File written to', testFile);
        console.log('File size:', fs.statSync(testFile).size);
        fs.unlinkSync(testFile);
    });
    stream.on('error', (err) => {
        console.error('ERROR writing stream:', err);
    });
} catch (err) {
    console.error('CATCH error:', err);
}
