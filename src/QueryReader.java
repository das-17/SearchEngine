import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
public class QueryReader{
    public static List<QueryType> getQueryList(String filename) {
        List<QueryType> queryList = new ArrayList<QueryType>();
        QueryType query = null;
        try { System.out.println("Reading queries....");
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/resources/"+filename));
            String line =  reader.readLine();
            boolean desc = false;
            while (line != null) {
                //System.out.println(line);
                if (line.startsWith("<top>")){
                    if (query != null){
                        queryList.add(query);
                    }
                    //System.out.println("New query created");
                    desc =false;
                    query = new QueryType();
                }
                else if (line.startsWith("<num>")){
                    String number =line.split(":")[1].trim();
                    //System.out.println("Number set created");
                    query.setNumber(number);

                }
                else if (line.startsWith("<title>")){
                    String title = line.split(">")[1].trim();
                   // System.out.println("Title created");
                    query.setTitle(title);
                }
                else if (line.startsWith("<desc>") ) {
                   // System.out.println("description created");
                    desc=true;
                }
                else if(line.startsWith("</top>"))
                {
                    desc=false;
                }
                else if(!line.startsWith("<desc>") && desc == true)
                    {query.setDescription(line.stripTrailing());;
                }
                line = reader.readLine();
            }
            reader.close();
        }
        catch(Exception e)
        {e.printStackTrace();}
        //System.out.println(queryList);
        return queryList;
    }
    public static void main(String args[])
    {
        System.out.println(getQueryList("xyz"));
    }

}