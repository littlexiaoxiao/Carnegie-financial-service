package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.genericdao.ConnectionPool;
import org.genericdao.DAOException;
import org.genericdao.GenericDAO;
import org.genericdao.MatchArg;
import org.genericdao.RollbackException;

import databean.FundPriceHistoryBean;

public class FundPriceHistoryDAO extends GenericDAO<FundPriceHistoryBean> {
    public FundPriceHistoryDAO(String tableName, ConnectionPool pool) throws DAOException {
        super(FundPriceHistoryBean.class, tableName, pool);
    }
    public FundPriceHistoryBean[] read(int fid) throws RollbackException{
        FundPriceHistoryBean[] fph = match(MatchArg.equals("fundId", fid));
        List<FundPriceHistoryBean> list = new ArrayList<FundPriceHistoryBean>(Arrays.asList(fph));
        
        Collections.sort(list, new Comparator<FundPriceHistoryBean>(){
            public int compare(FundPriceHistoryBean a, FundPriceHistoryBean b){
                java.util.Date da = a.getPriceDate();
                java.util.Date db = b.getPriceDate();
                if(da == null) return -1;
                if(db == null) return 1;
                return da.after(db) ? -1:1;
            }
        } );
        fph = list.toArray(fph);
        return fph;
    }
    
    public long getPriceByFundId(int fid) throws RollbackException{
        FundPriceHistoryBean[] fphs = match(MatchArg.equals("fundId", fid));
        if(fphs.length > 0){
            java.util.Date latest = fphs[0].getPriceDate();
            long price = fphs[0].getPrice();
            for(int j = 1; j < fphs.length; j ++){
                java.util.Date cur = fphs[j].getPriceDate();
                if( cur.after(latest) ){
                    price = fphs[j].getPrice();
                    latest = cur;  
                }
            }
            return price;
        }
        return 0;
    }
    
    public long getPriceByFundIdAndDate(int fid, Date d ) throws RollbackException{
        FundPriceHistoryBean[] fphs = match(MatchArg.and(
                                                MatchArg.equals("fundId", fid),
                                                MatchArg.equals("priceDate", d)) );
        if(fphs.length > 0){
            return fphs[0].getPrice();
        }
        return 0;
    }

    public void transitionDay(Date cur, int[] ids, double[] amount) throws RollbackException{
        for(int i = 0; i < ids.length; i ++){
            System.out.printf("In DAO, i=%d, %d, %f\n", i, ids[i], amount[i]);
            FundPriceHistoryBean fpb = new FundPriceHistoryBean();
            fpb.setFundId(ids[i]);
            fpb.setPrice((long)(amount[i]*100));
            fpb.setPriceDate(cur);
            create(fpb);
        }
    }

    public java.util.Date getAllLastDate() throws RollbackException {
        FundPriceHistoryBean[] fphs = match();

        if(fphs.length > 0){
            java.util.Date latest = null;
            for(int j = 0; j < fphs.length; j ++){
                java.util.Date cur = fphs[j].getPriceDate();
                if ( cur == null) 
                	continue;
                if ( latest == null || cur.after(latest) )
                    latest = cur;  
            }
            return latest;
        }
        return null;
    }
}
