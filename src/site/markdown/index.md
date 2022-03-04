# BAS API
API for calling the Bavarian Archive for Speech Signals (BAS) services:

http://hdl.handle.net/11858/00-1779-0000-0028-421B-4

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
[JavaDoc](apidocs/nzilbb/bas/BAS.html)

- MAUSBasic(String LANGUAGE, File SIGNAL, File TEXT):  
Invokes the MAUSBasic service, which combines G2P and MAUS for forced alignment given a WAV file and a plain text orthrogaphic transcript.
- G2P(String lng, String txt, String outsym, String featset, String oform, boolean syl, boolean stress):  
Invokes the G2P service for converting orthography into phonemic transcription.
- MAUS(String LANGUAGE, File SIGNAL, File BPF, String OUTFORMAT, String OUTSYMBOL):  
Invoke the general MAUS service, with mostly default options, for forced alignment given a WAV file and a phonemic transcription.
- Pho2Syl(String lng, File i, String tier, Boolean wsync, String oform, Integer rate):  
Invoke the Pho2Syl service to syllabify a phonemic transcription.
- TTS(String INPUT_TEXT):  
Convenience method to invoke the MaryTTS German Text-to-speech service with plain text input, with a WAV file as output, using the default voice.
- TextAlign(InputStream i, String cost, InputStream costfile, Boolean displc, String atype):  
Invoke the TextAlign service for aligning two representations of text, e.g. letters in orthographic transcript with phonemes in a phonemic transcription.
