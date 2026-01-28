# QuarryManager

JavaFX-põhine rakendus materjalide ja arvehaldussüsteemi jaoks. 
Võimaldab hallata firmasid, materjale, numbrimärke, hindu, ning genereerida PDF-arveid ja statistikat.

## Funktsioonid

- Firma kontaktide haldus
- Materjalide ja hindade haldus
- Numbrimärkide lisamine/kustutamine
- Arvete genereerimine PDF-formaadis koos automaatsete arvutustega (summa, käibemaks, kokku)
- Müügi statistika kuude lõikes

Kasutades:
- Java 17+
- JavaFX UI
- SQLite andmebaas
- Apache PDFBox PDF genereerimiseks

<img width="525" height="370" alt="image" src="https://github.com/user-attachments/assets/b7260def-0b25-4268-9e8f-8b4aef2083d2" />

## Käivitamine
1. Lae alla javaFX SDK
2. Klooni repo
3. Käivita main klass VM optionsitega: `--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml`
