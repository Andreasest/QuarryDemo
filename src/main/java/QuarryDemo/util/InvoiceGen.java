package QuarryDemo.util;

import QuarryDemo.model.Arve;
import QuarryDemo.model.Firma;
import QuarryDemo.model.Teenus;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;



public class InvoiceGen {

    public static boolean createInvoice(Arve arve, String salvestuskoht){
        Firma firma=arve.getFirma();
        int arvenr= arve.getArvenumber();


        int makseTingimus=firma.getMakseTingimus();
        List<Teenus> teenused=arve.getTeenused();

        String summa=arve.getSumma();
        String käibemaks=String.format("%.2f",Double.parseDouble(summa)*0.24).replace(",",".");
        String kokku=String.format("%.2f",Double.parseDouble(summa)*1.24).replace(",",".");
        LocalDate arveVäljastamisKuupäev=arve.getMakseTähtaeg();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);

            float margin = 50;
            float yStart = 750;
            float pageWidth = page.getMediaBox().getWidth();
            float usableWidth = pageWidth - 2 * margin;

            // Pilt
            try (InputStream is = InvoiceGen.class.getResourceAsStream("/images/logo.png")){
                if (is==null) throw new RuntimeException("Pilt puudub!");
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc,is.readAllBytes(),"logo");
                content.drawImage(pdImage, margin, yStart - 50, 100, 100);
            } catch (IOException e) {
                System.out.println("Logo faili ei leitud, jätkan ilma pildita.");
            }

            //  Kaheveeruline osa
            float twoColTop = yStart - 70;
            float colGap = 150;
            float colWidth = (usableWidth - colGap) / 2;

            //  arve saaja
            float leftColX = margin;
            float leftColY = twoColTop;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.newLineAtOffset(leftColX, leftColY);
            content.showText("Arve saaja: "+  firma.getName());
            content.endText();

            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 10);

            List<String> aadressiosad=lauseEraldaja(firma.getAddress(), 50);
            content.newLineAtOffset(leftColX, leftColY - 15);
            content.showText("                        "+aadressiosad.getFirst());
            for (int i = 1; i < aadressiosad.size(); i++) {
                content.newLineAtOffset(0, -15);
                content.showText("                        "+aadressiosad.get(i));
            }
            content.newLineAtOffset(0,-30);
            content.showText("Kontaktisik: "+firma.getSpokesperson());
            content.endText();

            // arve info
            float rightColX = leftColX + colWidth + colGap;
            float rightColY = twoColTop;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 12);
            content.newLineAtOffset(rightColX, rightColY);
            content.showText("         ARVE NR.   "+arvenr);
            content.endText();

            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 10);
            float lineHeight = 15;
            float currentY = rightColY - 15;
            content.newLineAtOffset(rightColX, currentY);
            content.showText("                Kuupäev:    "+ arveVäljastamisKuupäev.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            content.endText();

            content.beginText();
            content.newLineAtOffset(rightColX, currentY - lineHeight);
            content.showText("       Maksetingimus:   "+makseTingimus+" päeva");
            content.endText();

            content.beginText();
            content.newLineAtOffset(rightColX, currentY - 2*lineHeight);
            content.showText("        Maksetähtaeg:    "+arveVäljastamisKuupäev.plusDays(makseTingimus).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            content.endText();

            content.beginText();
            content.newLineAtOffset(rightColX, currentY - 3*lineHeight);
            content.showText("    Viivise % päevas:   "+"0.20");
            content.endText();

            // tabel
            float tableTopY = twoColTop - 150;
            float tableX = margin;
            float tableWidth = usableWidth;
            float rowHeight = 20;

            // Veergude laiused
            float col1Width = tableWidth * 0.5f; // Nimetus
            float col2Width = tableWidth * 0.1f;  // Ühik
            float col3Width = tableWidth * 0.1f; // Kogus
            float col4Width = tableWidth * 0.2f;  // Ühiku hind
            float col5Width = tableWidth * 0.1f;  // Summa

            // Tabeli päis
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            content.newLineAtOffset(tableX + 2, tableTopY);
            content.showText("Nimetus");
            content.newLineAtOffset(col1Width+10, 0);
            content.showText("Ühik");
            content.newLineAtOffset(col2Width-10, 0);
            content.showText("Kogus");
            content.newLineAtOffset(col3Width, 0);
            content.showText("Ühiku hind");
            content.newLineAtOffset(col4Width, 0);
            content.showText("Summa");
            content.endText();

            //  joon
            float headerLineY = tableTopY - 5;
            content.setLineWidth(1.5f);
            content.moveTo(tableX, headerLineY);
            content.lineTo(tableX + tableWidth, headerLineY);
            content.stroke();

            //  andmeread
            float textY = headerLineY - 15;
            content.setFont(PDType1Font.HELVETICA, 10);

            for (int i = 0; i < teenused.size(); i++) {
                float prgY = textY - i * rowHeight;
                float x = tableX + 2;

                Teenus teenus= teenused.get(i);
                String[] row={teenus.getMaterjal() + " - perioodil "+ teenus.getKuupäevaVahemik(),
                                teenus.getÜhik(),
                                NumberSõnadeks.formatDouble(teenus.getKogus()),
                                String.format(Locale.US, "%.2f", teenus.getHind()),
                                String.format(Locale.US, "%.2f", teenus.getSumma())};
                for (int j = 0; j < row.length; j++) {
                    content.beginText();
                    content.newLineAtOffset(x, prgY);
                    content.showText(row[j]);
                    content.endText();

                    if (j == 0) x += col1Width+15+5;
                    else if (j == 1) x += col2Width-3-7;
                    else if (j == 2) x += col3Width+12;
                    else if (j == 3) x += col4Width-19;
                }
            }


            //  Alumine  joon
            float bottomLineY = textY - (teenused.size() - 1) * rowHeight - 10;
            content.setLineWidth(1.5f);
            content.moveTo(tableX, bottomLineY);
            content.lineTo(tableX + tableWidth, bottomLineY);
            content.stroke();


            //  Kokkuvõtted
            float sumY = bottomLineY - 30;
            float rightAlignX = tableX + tableWidth - 150;
            //koordinaadid
            float sumTableX = rightAlignX;
            float sumTableY = sumY + 5;
            float sumTableWidth = 150;
            float sumRowHeight = 15;
            float coll1Width = sumTableWidth * 0.6f;
            float coll2Width = sumTableWidth * 0.4f;

            // summa km
            String[] labels1 = {"Summa km-ta:", "Käibemaks 24%:"};
            String[] values1 = {summa, käibemaks};

            // Kokku
            String label2 = "Kokku:";
            String value2 = String.valueOf(kokku);

            // Summa km-ta ja Käibemaks
            content.setFont(PDType1Font.HELVETICA, 10);
            for (int i = 0; i < labels1.length; i++) {
                float rowY = sumTableY - i * sumRowHeight;

                // Vasak veerg
                content.beginText();
                content.newLineAtOffset(sumTableX, rowY);
                content.showText(labels1[i]);
                content.endText();

                // Parem veerg, paremale joondatud
                float textWidth = PDType1Font.HELVETICA.getStringWidth(values1[i]) / 1000 * 10;
                content.beginText();
                content.newLineAtOffset(sumTableX + coll1Width + coll2Width - textWidth, rowY);
                content.showText(values1[i]);
                content.endText();
            }

            // Joon
            float lineY = sumTableY - labels1.length * sumRowHeight - 3+10;
            content.setLineWidth(1.5f);
            content.moveTo(sumTableX, lineY);
            content.lineTo(sumTableX + sumTableWidth, lineY);
            content.stroke();

            // Kokku
            float secondTableY = lineY - sumRowHeight - 3;
            content.setFont(PDType1Font.HELVETICA_BOLD, 10);

            // Vasak veerg
            content.beginText();
            content.newLineAtOffset(sumTableX, secondTableY);
            content.showText(label2);
            content.endText();

            // Parem veerg
            float lastTextWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(value2) / 1000 * 10;
            content.beginText();
            content.newLineAtOffset(sumTableX + coll1Width + coll2Width - lastTextWidth, secondTableY);
            content.showText(value2);
            content.endText();


            // Text
            float bottomTextY = sumY - 90;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 10);
            content.newLineAtOffset(margin, bottomTextY);
            content.showText("Summa sõnadega:");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin+100, bottomTextY);
            content.showText(NumberSõnadeks.convert(Double.parseDouble(kokku)));
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin, bottomTextY - 20);
            content.showText("Märkused:");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin+100, bottomTextY - 20);
            content.showText("Palun märkida maksekorraldusele makstava arve number.");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin, bottomTextY - 70);
            content.showText("Arve koostas: ");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin+100, bottomTextY - 70);
            content.showText("Firma OÜ");
            content.endText();


            content.beginText();
            content.newLineAtOffset(margin+100, bottomTextY - 85);
            content.showText("Mari Maasikas");
            content.endText();


            //  Firma andmed
            float footerY = 50;
            float footerColWidth = usableWidth / 3;
            String[] footerCols = {
                    "Firma OÜ",
                    "",""
            };
            String[] footerCols2 = {
                    "Riia 1, 50107 Tartu",
                    "Registrikood: 12345678",
                    "A/a: EE123456789101112131"
            };
            String[] footerCols3 = {
                    "Tel: +372 5555 5555",
                    "KMKR: EE123456789",
                    "Pank: LHV Pank "
            };
            String[] footerCols4 = {
                    "e-post: esindaja@firma.ee",
                    "",
                    ""
            };

            for (int col = 0; col < 3; col++) {
                float colX = margin + col * footerColWidth;
                float startY = footerY + 40;

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 10);
                content.newLineAtOffset(colX, startY);
                content.showText(footerCols[col]);
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 10);
                content.newLineAtOffset(colX, startY - 15);
                content.showText(footerCols2[col]);
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 10);
                content.newLineAtOffset(colX, startY - 30);
                content.showText(footerCols3[col]);
                content.endText();

                content.beginText();
                content.newLineAtOffset(colX, startY - 45);
                content.showText(footerCols4[col]);
                content.endText();
            }
            content.close();

            try {
                String filePath = salvestuskoht + "/arve_"+arvenr+"_"+firma.getName()+".pdf";

                doc.save(filePath);
                doc.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private static List<String> lauseEraldaja(String lause, int maxLen) {
        String[] parts = lause.split(", ");
        List<String> result = new ArrayList<>();

        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            String withComma = i < parts.length - 1 ? part + ", " : part;

            if (currentLine.length() + withComma.length() > maxLen) {
                result.add(currentLine.toString().trim());
                currentLine = new StringBuilder(withComma);
            } else {
                currentLine.append(withComma);
            }
        }

        if (!currentLine.isEmpty()) {
            result.add(currentLine.toString().trim());
        }

        return result;
    }
    public static boolean createCarList(Arve arve, String salvestuskoht) {
        try (PDDocument doc = new PDDocument()) {
            final float margin = 50;
            final float rowHeight = 15;
            final float pageHeight = PDRectangle.A4.getHeight();
            final float yStart = pageHeight - margin - 30;
            final float[] colWidths = {100, 150, 100, 150};
            boolean isFirstPage = true;

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream content = new PDPageContentStream(doc, page);
            float y = drawHeader(doc, page, content, arve, margin, yStart, colWidths, rowHeight, isFirstPage);


            content.setFont(PDType1Font.HELVETICA, 10);

            for (String[] rida : arve.getKogused()) {
                if (y < 2*margin + rowHeight * 4) {
                    content.close();

                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    content = new PDPageContentStream(doc, page);

                    isFirstPage = false;
                    y = drawHeader(doc, page, content, arve, margin, yStart, colWidths, rowHeight, isFirstPage);
                    content.setFont(PDType1Font.HELVETICA, 10);
                }

                // joonista rida
                float nextX = margin;
                for (int i = 0; i < rida.length; i++) {
                    drawCell(content, rida[i], nextX, y, colWidths[i], rowHeight);
                    nextX += colWidths[i];
                }
                y -= rowHeight;
            }


            y -= 20;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            content.newLineAtOffset(margin + 200, y);
            content.showText("KOKKU");
            content.endText();
            for (Teenus teenus:arve.getTeenused()){
                content.beginText();
                content.newLineAtOffset(margin + 260, y);
                y-=15;
                content.showText(teenus.getMaterjal()+": "+teenus.getKogus()+" t");
                content.endText();
            }


            // Jalus
            y -= 40;
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 10);
            content.newLineAtOffset(margin, y);
            content.showText("Lugupidamisega,");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin, y - 15);
            content.showText("Firma OÜ");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin, y - 30);
            content.showText("Mari Maasikas");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin, y - 45);
            content.showText("+327 5555 5555");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin, y - 60);
            content.showText("esindaja@firma.ee");
            content.endText();

            content.close();

            File targetFolder = new File(salvestuskoht + File.separator + arve.getFirma().getName());
            System.out.println(targetFolder);

            String fileName = arve.getLõpp() + "_" + arve.getFirma().getName() + ".pdf";

            File outputFile;
            if (targetFolder.exists() && targetFolder.isDirectory()) {
                outputFile = new File(targetFolder, fileName);
            } else {
                outputFile = new File(salvestuskoht, fileName); // fallback
            }

            doc.save(outputFile);
            doc.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private static float drawHeader(PDDocument doc, PDPage page, PDPageContentStream content,
                                    Arve arve, float margin, float yStart,
                                    float[] colWidths, float rowHeight, boolean isFirstPage) throws IOException {
        content.setFont(PDType1Font.HELVETICA, 10);

        float y = yStart;

        if (isFirstPage) {
            // Firma nimi
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText(arve.getFirma().getName());
            content.endText();

            // Firma email
            content.beginText();
            content.newLineAtOffset(margin, y - 15);
            content.showText(arve.getFirma().getEmail());
            content.endText();

            // Kuupäev paremas nurgas
            content.beginText();
            content.newLineAtOffset(margin + 400, y - 15);
            //content.showText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            content.showText(arve.getLõpp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            content.endText();

            // Selgitus
            content.beginText();
            content.newLineAtOffset(margin, y - 40);
            content.showText("Karjäärist väljastatud materjal " + arve.getFirma().getName() + " poolt volitatud sõidukitele ja vedajatele");
            content.endText();

            content.beginText();
            content.newLineAtOffset(margin, y - 55);
            content.showText("perioodil " + arve.getPeriood() + ".");
            content.endText();

            y -= 80;
        } else {
            y -= 20; // väike vahe järgmise lehe ülaosaga
        }

        // Tabeli päis
        content.setFont(PDType1Font.HELVETICA_BOLD, 10);
        String[] headers = new String[]{"KUUPÄEV", "SÕIDUK/VEDAJA", "KOGUS KOKKU (t)", "MATERJAL"};
        float nextX = margin;

        for (int i = 0; i < headers.length; i++) {
            drawCell(content, headers[i], nextX, y, colWidths[i], rowHeight);
            nextX += colWidths[i];
        }

        return y - rowHeight; // järgmise rea Y
    }

    private static void drawCell(PDPageContentStream contentStream, String text,
                                 float x, float y, float width, float height) throws IOException {
        // Ristkülik
        contentStream.addRect(x, y - height, width, height);
        contentStream.stroke();

        // Texti laius
        float fontSize = 10;
        float textWidth = PDType1Font.HELVETICA.getStringWidth(text) / 1000 * fontSize;

        float textX = x + (width - textWidth) / 2;
        float textY = y - height / 2 - (fontSize / 4);
        contentStream.beginText();
        contentStream.newLineAtOffset(textX, textY);
        contentStream.showText(text);
        contentStream.endText();
    }
}
