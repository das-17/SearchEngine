import java.util.*;
import java.util.stream.Collectors;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;

import org.apache.lucene.queryparser.classic.QueryParser;

public class SearchEngine {
    public static String corpusFile = "ohsumed.88-91";
    public static String queryFile = "query.ohsu.1-63";
    public static IndexGenerator igen = new IndexGenerator();
    public static void main(String args[]) throws IOException
    {
        System.out.println("Search_Engine booting up.");
        SearchEngine se=new SearchEngine();
        Directory index = igen.indexCreator(corpusFile, false);
        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        se.search(index, reader, searcher,args[0]);
    }
    public List<String> pseudoRelevance(IndexSearcher searcher, TopDocs topDocs, QueryType query) throws IOException
    {  HashMap<String, Integer> termfreq = new HashMap<>();
        
        for (final ScoreDoc scoreDoc : topDocs.scoreDocs) {
        {String[] keywords=searcher.doc(scoreDoc.doc).getField(igen.KEYWORDS_FIELD).stringValue().split(";");
                            for(String str:keywords)
                            {
                                if(termfreq.containsKey(str))
                                {
                                    termfreq.put(str,termfreq.get(str)+1);
                                }
                                else
                                {
                                    termfreq.put(str,1);
                                }
                            }
        }}
        if(termfreq.size()>0)
                   { Map<String,Integer> topterms = 
                    termfreq.entrySet().stream()
                       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                       .limit(5)
                       .collect(Collectors.toMap(
                          Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                          return new ArrayList<>(topterms.keySet());
                     
                   }
                   return new ArrayList<>();
                  
        
    }
    public  void search(Directory index, DirectoryReader reader, IndexSearcher searcher, String algorithm)
    {
     
     try {
    
        String model;
        int retreival=50;
        if(algorithm.equals("boolean"))
          {searcher.setSimilarity(new BooleanSimilarity());model="BooleanSimilarity";}

        else if(algorithm.equals("tf"))
         {searcher.setSimilarity(new TFSimilarity());model="TFSimilarity";}

        else if(algorithm.equals("tfidf"))
         {searcher.setSimilarity(new ClassicSimilarity());model="TFIDFSimilarity";}

         else if(algorithm.equals("relevance"))
         {searcher.setSimilarity(new BM25Similarity());
          index = igen.indexCreator(corpusFile, true);
          reader = DirectoryReader.open(index);
          searcher=new IndexSearcher(reader);
          retreival=20;
          model="BM25-PseudoRelevance";
        }

        else if(algorithm.equals("custom"))
         {searcher.setSimilarity(new BM25Similarity());
            index = igen.indexCreator(corpusFile, true);
            reader = DirectoryReader.open(index);
            searcher=new IndexSearcher(reader);
            retreival=20;
            model="PRTF25";
        }
        else
        {
            System.out.println("Default algorithm:BM25");
            searcher.setSimilarity(new BM25Similarity());
            algorithm = "BM25";
            model="BM25";
        }
        
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(System.getProperty("user.dir")+"/resources/"+algorithm),StandardOpenOption.TRUNCATE_EXISTING);
        final Analyzer analyzer = new EnglishAnalyzer();
        QueryParser queryParser = new MultiFieldQueryParser(new String[]{"title", "source", "abstract", "author", "publication", "keywords"}, analyzer);
        long startTime=System.currentTimeMillis();
        List<QueryType> queries = QueryReader.getQueryList(queryFile);
        int count = 0;
        
        System.out.println("Number of queries"+queries.size());
        
        for (QueryType query : queries) {
                    Query descpart = queryParser.parse(QueryParser.escape(query.description));
                    TopDocs topDocs = searcher.search(descpart, retreival);
                    if(algorithm.equals("relevance"))
                    {
                        List<String> keywords=this.pseudoRelevance(searcher, topDocs, query);
                        for(String str: keywords)
                        {
                            query.description=query.description+" "+str;
                        }
                        descpart = queryParser.parse(QueryParser.escape(query.description));
                        retreival=50;
                        topDocs = searcher.search(descpart, retreival);
                        
                    }
                    else if(algorithm.equals("Custom"))
                    {
                        List<String> keywords=this.pseudoRelevance(searcher, topDocs, query);
                        for(String str: keywords)
                        {
                            query.description=query.description+" "+str;
                        }
                        descpart = queryParser.parse(QueryParser.escape(query.description));
                        index = igen.indexCreator(corpusFile, false);
                        reader = DirectoryReader.open(index);
                        searcher=new IndexSearcher(reader);
                        retreival=50;
                        searcher.setSimilarity(new TFSimilarity());
                        topDocs = searcher.search(descpart, retreival);  
                    }
                    
                    count = 1;
                    for (final ScoreDoc scoreDoc : topDocs.scoreDocs) {
                        writer.write(MessageFormat.format("{0}\tQ0\t{1}\t{2}\t{3}\t{4}\n",
                        query.getNumber(), searcher.doc(scoreDoc.doc).getField(igen.DOC_FIELD).stringValue(),
                        count, scoreDoc.score, model));
                        count+=1;
                    }

                    
                }
                System.out.println("Results printed out.");
                long endTime=System.currentTimeMillis();
                System.out.println("Time taken:"+(endTime-startTime)+"ms");
                writer.close();
    } 
    
    catch (Exception e) {
        e.printStackTrace();
    }
  } 
}
