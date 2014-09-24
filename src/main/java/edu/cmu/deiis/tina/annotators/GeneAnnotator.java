package edu.cmu.deiis.tina.annotators;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.deiis.types.GeneTrueAnswerAnnotation;
import edu.cmu.deiis.types.SentenceAnnotation;
import edu.cmu.deiis.types.TaggedGeneAnnotation;

public class GeneAnnotator extends JCasAnnotator_ImplBase {
  /**
   * @see JCasAnnotator_ImplBase#initialize(JCas) The overrided initialize() function that reads all
   *      the hmm model path config for gene tagging.
   */
  /**
   * an alternative to parsing (shallow) that provides a partial syntactic structure of a sentence
   * load a Chunker instance to tag data
   */
  private Chunker chunker;
  /**
   * Initializes this resource
   * 
   * @throws ResourceInstantiationException
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    //System.out.println("GeneAnnotator Initialize In-------------!");
    /**
     * call the super method from UIMA
     */
    super.initialize(aContext);
    // Get config. parameter values
    String path = (String)aContext.getConfigParameterValue("ModelPath");
    File modelFile = new File(path);
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println("Failure of hmm model loading!");
    }
  }
  /**
   * @see JCasAnnotator_ImplBase#process(JCas); The overrided process() function that processes jcas
   *      into gene annotations and creates new GeneAnnotation.
   */
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // TODO Auto-generated method stub
    // TODO Auto-generated method stub
    // Get the input sentence annotations from jcas
    FSIndex sentenceIndex = aJCas.getAnnotationIndex(SentenceAnnotation.type);

    // Iterator to get each sentence annotation
    Iterator sentenceIter = sentenceIndex.iterator();

    while (sentenceIter.hasNext()) {
      // Get the content of each sentence
      SentenceAnnotation sentence = (SentenceAnnotation) sentenceIter.next();
      String content = sentence.getCoveredText();
      content = content.trim();
      
      /**
       * Check if the annotation match the pattern
       */
      String[] data = null;
      if(content.contains("|")){
        data = content.split("\\|");
        
        /**
         * Calculate the offset to get the data from sentences
         */
        int offset = sentence.getBegin() + data[0].length() + 1 + data[1].length() + 1;
        GeneTrueAnswerAnnotation gene = new GeneTrueAnswerAnnotation(aJCas);
        int start = offset;
        int end = offset + data[2].length();
        
        /**
         * find the target gene and do our job!
         */
        gene.setBegin(start);
        gene.setEnd(end);
        gene.setCasProcessorId("GeneAnnotator");
        gene.setConfidence(1);
        gene.setId(data[0]);
        gene.addToIndexes();       
      }else{
        data = content.split("\\s");
        String s = content.substring(data[0].length() + 1);
        Chunking chunking = chunker.chunk(s);
        Set<Chunk> set = chunking.chunkSet();
        
        /**
         * record the tagged genes into our output
         */
        for(Chunk c: set){
          TaggedGeneAnnotation gene = new TaggedGeneAnnotation(aJCas);
          int start = sentence.getBegin() + c.start() + data[0].length() + 1;
          int end = sentence.getBegin() + c.end() + data[0].length() + 1;
          
          gene.setBegin(start);
          gene.setEnd(end);
          gene.setCasProcessorId("GeneAnnotator");
          gene.setConfidence(c.score());
          gene.setSentence(sentence);
          gene.setId(data[0]);
          gene.addToIndexes();
        }
      }
    }
  }
}
