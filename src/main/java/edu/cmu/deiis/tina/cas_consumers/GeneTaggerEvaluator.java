package edu.cmu.deiis.tina.cas_consumers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

import edu.cmu.deiis.types.GeneTrueAnswerAnnotation;
import edu.cmu.deiis.types.TaggedGeneAnnotation;

/**
 * An Evaluator annotator will evaluator all the score of each answer and display the result in
 * standard output.
 */

public class GeneTaggerEvaluator extends CasConsumer_ImplBase {
  
  /**
   * Class member, a static variable that refers to the question number.
   */
  private JCas jcas;

  private HashSet<String> trueGeneSet;

  private HashSet<String> labeledGeneSet;

  private Map<String, Integer> res;

  /**
   * Initializes this resource
   * 
   * @throws ResourceInstantiationException
   */
  public void initialize() throws ResourceInitializationException {
    trueGeneSet = new HashSet<String>();
    labeledGeneSet = new HashSet<String>();
    res = new HashMap<String, Integer>();
  }

  /**
   * @see JCasAnnotator_ImplBase#process(JCas); The overrided process function that processes jcas
   *      into sentence annotations
   */
  public void processCas(CAS aCas) throws ResourceProcessException {
    // TODO Auto-generated method stub
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    /**
     * mark the position of our gold standard
     */
    FSIndex trueIndex = jcas.getAnnotationIndex(GeneTrueAnswerAnnotation.type);

    // Iterator to get each true gene annotation
    Iterator trueIter = trueIndex.iterator();
    while (trueIter.hasNext()) {
      // Get the content of each sentence
      GeneTrueAnswerAnnotation trueGene = (GeneTrueAnswerAnnotation) trueIter.next();
      trueGeneSet.add(trueGene.getCoveredText());
    }
    /**
     * mark the position of our prediction - output
     */
    FSIndex labeledIndex = jcas.getAnnotationIndex(TaggedGeneAnnotation.type);
    // Iterator to get each labeled gene annotation
    Iterator labeledIter = labeledIndex.iterator();

    while (labeledIter.hasNext()) {
      // Get the content of each sentence
      TaggedGeneAnnotation labeledGene = (TaggedGeneAnnotation) labeledIter.next();
      labeledGeneSet.add(labeledGene.getCoveredText());
      
      /**
       * record the position and id of our result
       */
      int base = labeledGene.getSentence().getBegin();
      int begin = labeledGene.getBegin() - base;
      int end = labeledGene.getEnd() - base;
      String s = labeledGene.getId() + "|" + begin + " " + end  + "|" + labeledGene.getCoveredText();
      res.put(s, base);
    }
    //printResult();
    return;
  }

  /**
   * @see JCasAnnotator_ImplBase#process(JCas); The overrided process function that processes jcas
   *      into sentence annotations
   */
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);
    printResult();
  }
  
  private void printResult() throws FileNotFoundException, UnsupportedEncodingException{
    List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>( res.entrySet() );
    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
      public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
        return e1.getValue() - e2.getValue();
      }
    });

    PrintWriter writer = new PrintWriter("./src/main/resources/output/text.out", "UTF-8");
    for(int i = 0; i < list.size(); i++)
      writer.println(list.get(i).getKey());
    writer.close();
    
    int bingo = 0;
    int totalP = labeledGeneSet.size();
    int totalR = trueGeneSet.size();

    for (String s : labeledGeneSet) {
      if (trueGeneSet.contains(s))
        bingo++;
    }
    
    double recall = (double) bingo / totalR;
    double precision = (double) bingo / totalP;
    double f_measure = (double) 2 * recall * precision / (recall + precision);
    
    System.out.println("----------------------------------------------------");
    System.out.println("****************************************************");
    System.out.println("----------------------------------------------------");
    System.out.println("System Performance Evaluation:");
    System.out.println("Precision: " + precision);
    System.out.println("Recall: " + recall);
    System.out.println("F_measure: " + f_measure);
    System.out.println();
  }
}
