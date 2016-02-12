package edu.ucla.cs.cs144;

import java.io.IOException;
import java.io.StringReader;
import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
    
    private static String INDEX_DIR = "/var/lib/lucene/index1";
    /** Creates a new instance of Indexer */
    public Indexer() {
    }
 
    public void rebuildIndexes(){

        Connection conn = null;

        // create a connection to the database to retrieve Items from MySQL
	try {
	    conn = DbManager.getConnection(true);
	} catch (SQLException ex) {
	    System.out.println(ex);
	}


	/*
	 * Add your code here to retrieve Items using the connection
	 * and add corresponding entries to your Lucene inverted indexes.
         *
         * You will have to use JDBC API to retrieve MySQL data from Java.
         * Read our tutorial on JDBC if you do not know how to use JDBC.
         *
         * You will also have to use Lucene IndexWriter and Document
         * classes to create an index and populate it with Items data.
         * Read our tutorial on Lucene as well if you don't know how.
         *
         * As part of this development, you may want to add 
         * new methods and create additional Java classes. 
         * If you create new classes, make sure that
         * the classes become part of "edu.ucla.cs.cs144" package
         * and place your class source files at src/edu/ucla/cs/cs144/.
	 * 
	 */
    try{
        Statement s = conn.createStatement();
        Directory indexDir = FSDirectory.open(new File(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(indexDir, config);
        
        ResultSet ItemidDesc = s.executeQuery("Select ItemID, Name, Description From ItemTable");
        while(ItemidDesc.next()){
            Document doc = new Document();
            String ID = ItemidDesc.getString("ItemID");
            String name = ItemidDesc.getString("Name");
            String description = ItemidDesc.getString("Description");
            Statement catS = conn.createStatement();
            ResultSet category = catS.executeQuery("Select Category From ItemCategory Where ItemID="+ItemidDesc.getString("ItemID"));
            String allCategories="";
            while(category.next()){
                allCategories += category.getString("Category")+" ";
            }
            doc.add(new StringField("ID", ID, Field.Store.YES));
            doc.add(new StringField("Name", name, Field.Store.YES));
            String content = name+" "+allCategories+description;
            doc.add(new TextField("Content", content, Field.Store.NO));
            indexWriter.addDocument(doc);
            //System.out.println("Processed "+ID);
        }
        
        indexWriter.close();
        
    } catch (SQLException ex){
        System.out.println("SQLException caught");
        System.out.println("---");
        while ( ex != null ){
            System.out.println("Message   : " + ex.getMessage());
            System.out.println("SQLState  : " + ex.getSQLState());
            System.out.println("ErrorCode : " + ex.getErrorCode());
            System.out.println("---");
            ex = ex.getNextException();
        }
    } catch (IOException io){
        System.out.println("I/O Exception");
    }
        // close the database connection
	try {
	    conn.close();
	} catch (SQLException ex) {
	    System.out.println(ex);
	}
    }    

    public static void main(String args[]) {
        Indexer idx = new Indexer();
        idx.rebuildIndexes();
    }   
}
