package edu.cmu.deiis.tina.annotators;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.SentenceAnnotation;

/**
 * The sentence annotator that create SentenceAnnotations. A SentenceAnnotation annotates a
 * sentence of line.
 */

public class SentenceAnnotator extends JCasAnnotator_ImplBase {
  /**
   * Abstract class can be implemented by the specific class later
   */
  static abstract class Maker {
    abstract Annotation newAnnotation(JCas jcas, int start, int end);
  }

  /**
   * Class member that refers to the jCas in the pipeline.
   */
  JCas jcas;
  
  /**
   * Class member that contains the whole content of a document in the jCas.
   */
  String input;

  // *********************************************
  // * function pointers for new instances *
  // *********************************************
  
  /**
   * Factory method that returns a new SentenceAnnotation instance
   */
  static final Maker sentenceAnnotationMaker = new Maker() {
    Annotation newAnnotation(JCas jcas, int start, int end) {
      return new SentenceAnnotation(jcas, start, end);
    }
  };

  // *************************************************************
  // * process *
  // *************************************************************
  
  /**
   * @see JCasAnnotator_ImplBase#process(JCas); The overrided process function that processes jcas
   *      into sentence annotations
   */
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    jcas = aJCas;
    input = jcas.getDocumentText();

    // Create Annotations
    makeAnnotations(sentenceAnnotationMaker, aJCas);
  }

  // *************************************************************
  // * Helper Methods *
  // *************************************************************
  
  /**
   * Helper Method that marks each sentence's end and extract each sentence, which is copied from
   * UIMA-Example project.
   */
  void makeAnnotations(Maker m, JCas jCas) {
    String[] lines = input.split("\n");
    int offset = 0;
    for(int i = 0; i < lines.length; i++){
      int temp = lines[i].length();
      SentenceAnnotation annotation = (SentenceAnnotation) m.newAnnotation(jCas, offset, offset + lines[i].length());
      annotation.setCasProcessorId("SentenceAnnotator");
      annotation.setConfidence(1.0);
      annotation.addToIndexes();
      offset = offset + lines[i].length() + 1;
    }
    return;
  }

}
