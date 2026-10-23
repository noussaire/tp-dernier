package com.example.util;
import javax.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import java.nio.file.*;
import java.io.IOException;
public class PerformanceReport {
    private final Statistics stats;
    public PerformanceReport(EntityManagerFactory emf) { stats=emf.unwrap(SessionFactory.class).getStatistics(); }
    public String render() {
        long total=0; for(String query:stats.getQueries()) total+=stats.getQueryStatistics(query).getExecutionTotalTime();
        return "=== RAPPORT DE PERFORMANCE ===\nRequetes executees : "+stats.getQueryExecutionCount()
            +"\nCache L2 hits : "+stats.getSecondLevelCacheHitCount()+"\nCache L2 misses : "+stats.getSecondLevelCacheMissCount()
            +"\nCache L2 puts : "+stats.getSecondLevelCachePutCount()+"\nCache requetes hits : "+stats.getQueryCacheHitCount()
            +"\nTransactions : "+stats.getTransactionCount()+"\nTransactions reussies : "+stats.getSuccessfulTransactionCount()
            +"\nStatements JDBC prepares : "+stats.getPrepareStatementCount()+"\nDuree cumulee des requetes suivies (ms) : "+total
            +"\nNote : les find() et les ecritures ne sont pas inclus dans la duree JPQL.\n";
    }
    public void printReport() { System.out.println(render()); }
    public void save(Path path) throws IOException { Files.createDirectories(path.getParent()); Files.writeString(path,render()); }
}
