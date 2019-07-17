# BAS API
API for calling the Bavarian Archive for Speech Signals (BAS) services:

http://hdl.handle.net/11858/00-1779-0000-0028-421B-4

Currently there is an API for Java, if we get time there might later be one for javascript and Python.

## Usage

You need to `import nzilbb.bas.BAS;` and then instantiate a `BAS` object:
```
BAS bas = new BAS();
```

Once that's done, you can invoke the function you need, and check/retrieve the results, e.g.:
```
BASResponse response = bas.MAUSBasic("eng-NZ", new File("my.wav"), new File("my.txt"));
if (response.getWarnings() != null) System.out.println(response.getWarnings());
if (response.getSuccess())
{
   response.saveDownload(new File("my.TextGrid"))
}
```

## API

Below are the basic functions. For convenience functions and other options, check the
[JavaDoc](https://htmlpreview.github.io/?https://github.com/nzilbb/bas/blob/master/java/javadoc/nzilbb/bas/BAS.html)

### MAUSBasic(String LANGUAGE, File SIGNAL, File TEXT)

Invokes the MAUSBasic service, which combines G2P and MAUS for forced alignment given a WAV file and a plain text orthrogaphic transcript.
- *LANGUAGE* [RFC 5646](https://tools.ietf.org/html/rfc5646) tag for identifying the language.
- *SIGNAL* The signal, in WAV format.
- *TEXT* The transcription of the text.

### G2P(String lng, String txt, String outsym, String featset, String oform, boolean syl, boolean stress)
Invokes the G2P service for converting orthography into phonemic transcription.

This convenience method takes a String as the text, and assumes *iform* = "txt".
- *lng* [RFC 5646](https://tools.ietf.org/html/rfc5646) tag for identifying the language.
- *txt* The text to transform as a String.
- *outsym* Ouput phoneme symbol inventory:
  + "sampa"  + language-specific SAMPA variant is the default.
  + "x-sampa"  + language independent X-SAMPA and IPA can be chosen.
  + "maus-sampa"  + maps the output to a language-specific phoneme subset that WEBMAUS can process.
  + "ipa"  + Unicode-encoded IPA.
  + "arpabet"  + supported for eng-US only
- *featset*  + Feature set used for grapheme-phoneme conversion. 
  + "standard" comprises a letter window centered on the grapheme to be converted.
  + "extended" set additionally includes part of speech and morphological analyses.</li>
- *oform* Output format:
  + "bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier.
  + "bpfs" differs from "bpf" only in that respect, that the phonemes are separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require the additional parameters "tgrate" and "tgitem". The content of the TextGrid tier "tgitem" is stored as a word chunk segmentation in the partiture tier TRN.
  + "txt" indicates a replacement of the input words by their transcriptions; single line output without punctuation, where phonemes are separated by blanks and words by tabulators.
  + "tab" returns the grapheme phoneme conversion result in form of a table with two columns. The first column comprises the words, the second column their blank-separated transcriptions.
  + "exttab" results in a 5-column table. The columns contain from left to right: words, transcriptions, part of speech, morpheme segmentations, and morpheme class segmentations.
  + "lex" transforms the table to a lexicon, i.e. words are unique and sorted.
  + "extlex" provides the same information as "exttab" in a unique and sorted manner. For all lex and tab outputs columns are separated by ';'.
  + "exttcf" which is currently available for German and English only additionally adds part of speech (STTS tagset), morphs, and morph classes.
  + With "tg" and "exttg" TextGrid output is produced.
- *syl* whether or not word stress is to be added to the output transcription. 
- *stress* whether or not the output transcription is to be syllabified. 

### MAUS(String LANGUAGE, File SIGNAL, File BPF, String OUTFORMAT, String OUTSYMBOL)

Invoke the general MAUS service, with mostly default options, for forced alignment given a WAV file and a phonemic transcription.
- *LANGUAGE* [RFC 5646](https://tools.ietf.org/html/rfc5646) tag for identifying the language.
- *SIGNAL* The signal, in WAV format.
- *BPF* Phonemic transcription of the utterance to be segmented. Format is a [BAS Partitur Format (BPF)](http://www.bas.uni-muenchen.de/forschung/Bas/BasFormatseng.html) file with a KAN tier.
- *OUTFORMAT* Defines the output format:
  + "TextGrid"  + a praat compatible TextGrid file
  + "par" or "mau-append"  + the input BPF file with a new (or replaced) tier MAU
  + "csv" or "mau"  + only the BPF MAU tier (CSV table)
  + "legacyEMU"  + a file with extension *.EMU that contains in the first part the Emu hlb file (*.hlb) and in the second part the Emu phonetic segmentation (*.phonetic)
  + "emuR"  + an Emu compatible *_annot.json file</li>
- *OUTSYMBOL* Defines the encoding of phonetic symbols in output. 
  + "sampa"  + (default), phonetic symbols are encoded in language specific SAM-PA (with some coding differences to official SAM-PA
  + "ipa"  + the service produces UTF-8 IPA output.
  + "manner"  + the service produces IPA manner of articulation for each segment; possible values are: silence, vowel, diphthong, plosive, nasal, fricative, affricate, approximant, lateral-approximant, ejective.
  + "place"  + the service produces IPA place of articulation for each segment; possible values are: silence, labial, dental, alveolar, post-alveolar, palatal, velar, uvular, glottal, front, central, back.

### Pho2Syl(String lng, File i, String tier, Boolean wsync, String oform, Integer rate)

Invoke the Pho2Syl service to syllabify a phonemic transcription.
- *lng* [RFC 5646](https://tools.ietf.org/html/rfc5646) tag for identifying the language.
- *i* Phonemic transcription of the utterance to be segmented. Format is a [BAS Partitur Format (BPF)](http://www.bas.uni-muenchen.de/forschung/Bas/BasFormatseng.html) file with a KAN tier.
- *tier* Name of tier in the annotation file, whose content is to be syllabified.
- *wsync* Whether each word boundary is considered as syllable boundary.
- *oform* Output format:
  + "bpf"  + BAS Partiture format
  + "tg"  + TextGrid format</li>
- *rate* Only needed if *oform* = "tg" (TextGrid); Sample rate to convert sample values from BAS partiture file to seconds in TextGrid. 

### TTS(String INPUT_TEXT)

Convenience method to invoke the MaryTTS German Text-to-speech service with plain text input, with a WAV file as output, using the default voice.
- *INPUT_TEXT* The text input.

### TextAlign(InputStream i, String cost, InputStream costfile, Boolean displc, String atype)

Invoke the TextAlign service for aligning two representations of text, e.g. letters in orthographic transcript with phonemes in a phonemic transcription.
- *i* CSV text file with two semicolon-separated columns. Each row contains a sequence pair to be aligned. The sequence elements must be separated by a blank. Example: a word and its canonical transcription like S c h e r z;S E6 t s.
- *cost* Cost function for the edit operations substitution, deletion, and insertion to be used for the alignment.
  + "naive"  + assigns cost 1 to all operations except of null-substitution, i.e. the substitution of a symbol by itself, which receives cost 0. This 'naive' cost function should be used only if the pairs to be aligned share the same vocabulary, which is NOT the case e.g. in grapheme-phoneme alignment (grapheme 'x' is not the same as phoneme 'x').
  + "g2p_deu", "g2p_eng" etc. are predefined cost functions for grapheme-phoneme alignment for the respective language expressed as iso639-3.
  + "intrinsic"  +  a cost function is trained on the input data and returned in the output zip. Costs are derived from co-occurrence probabilities, thus the bigger the input file, the more reliable the emerging cost function.
  + "import"  + the user can provide his/her own cost function file, that must be a semicolon-separated 3-column csv text file. Examples: v;w;0.7  + the substitution of 'v' by 'w' costs 0.7. v;_;0.8  + the delition of 'v' costs 0.8; _;w;0.9  + the insertion of 'w' costs 0.9. A typical usecase is to train a cost function on a big data set with cost='intrinsic', and to subsequently apply this cost function on smaller data sets with cost='import'.
- *costfile* CSV text file with three semicolon-separated columns. Each row contains three columns of the form a;b;c, where c denotes the cost for substituting a by b. Insertion and deletion are are marked by an underscore.
- *displc* whether alignment costs should be displayed in a third column in the output file. 
- *atype* Alignment type:
  + "dir"  + align the second column to the first.
  + "sym" symmetric alignment.

## BASResponse

Each method returns a BASResponse object, which you can interrogate to get the result of the request, which is summarized below.

Check the 
[JavaDoc](https://htmlpreview.github.io/?https://github.com/nzilbb/bas/blob/master/java/javadoc/nzilbb/bas/BASResponse.html)
for more details.


### getSuccess()

true if successful, false otherwise.

### getDownloadLink()

URL for downloading result.

### getOutput()

Output message.

### getWarnings()

Warning messages.

### getXml()

Original XML of the response.

### saveDownload() / saveDownload(File file)

Convenience function for downloading the result, if any.

Returns a File object.
