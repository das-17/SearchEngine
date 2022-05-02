import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

public class IndexGenerator {
     String INSID = ".I";
     String DOCID = ".U";
     String DOC_FIELD = "document_identifier";
     String KEYWORDS = ".M";
     String KEYWORDS_FIELD = "keywords";
     String TITLE = ".T";
     String TITLE_FIELD = "title";

     String PUBLICATION = ".P";
     String PUBLICATION_FIELD = "publication";
     String ABS = ".W";
     String ABS_FIELD = "abstract";
     String AUTHOR = ".A";
     String AUT_FIELD = "author";
     String SRC = ".S";
     String SRC_FIELD = "source";
    public Directory indexCreator(String filename, Boolean feedback) throws IOException{
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/resources/"+filename));
        IndexWriter writer = new IndexWriter(index, config);
        try
        {
          
          //int dcount = 0;
          String line = reader.readLine();
          Document curr = null;
          String field = null;
          Field.Store store = Field.Store.NO;
          while(line != null)
          {

            if (line.startsWith(INSID)) {
                if (curr != null) {
                    writer.addDocument(curr);
                }
                //dcount+=1;
                curr = new Document();

            } else if (line.startsWith(DOCID)) {
                field = DOC_FIELD;
                store = Field.Store.YES;
            } else if (line.startsWith(KEYWORDS)) {
                field = KEYWORDS_FIELD;
                if(feedback = true)
                  {store = Field.Store.YES;}
                else
                  {store = Field.Store.NO;}

            } else if (line.startsWith(TITLE)) {
                field = TITLE_FIELD;
                store = Field.Store.NO;

            } else if (line.startsWith(PUBLICATION)) {
                field = PUBLICATION_FIELD;
                store = Field.Store.NO;


            } else if (line.startsWith(ABS)) {
                field = ABS_FIELD;
                store = Field.Store.NO;

            } else if (line.startsWith(SRC)) {
                field = SRC_FIELD;
                store = Field.Store.NO;

            } else {
                curr.add(new TextField(field, line, store));
            }
            line = reader.readLine();
            }
        writer.addDocument(curr);
        writer.commit();
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            reader.close();
            writer.close();
        }
        return index;

    }
}
