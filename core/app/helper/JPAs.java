package helper;

import org.hibernate.ejb.HibernateQuery;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import play.db.helper.SqlSelect;
import play.db.jpa.JPA;

import javax.persistence.Query;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 5:33 PM
 */
public class JPAs {

    /**
     * <pre>
     * 返回的结果按照 as 作为 key 进行返回 List[Map[key,val]];
     * PS: 从不同的 Modal 中寻找出所需要的 Map 最好用, 使用 key 固定值
     * ex: new JpqlSelect().select("oi.product.sku as sku, oi.order.orderId as orderId, oi.quantity as qty")
     *                     .from("OrderItem oi").where("oi.createDate>=?").param(DateTime.parse("2012-06-01").toDate()
     * </pre>
     *
     * @param hql
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends SqlSelect> Query createQueryMap(T hql) {
        return basicCreateQuery(hql, Transformers.ALIAS_TO_ENTITY_MAP);
    }

    /**
     * 返回结果按照 List 返回, List[List[..]]
     *
     * @param hql
     * @param <T>
     * @return
     */
    public static <T extends SqlSelect> Query createQueryList(T hql) {
        return basicCreateQuery(hql, Transformers.TO_LIST);
    }

    /**
     * 返回结果按照 Object[] 返回, Object[Object..]
     * PS: 作为单个结果返回的时候最好用
     *
     * @param hql
     * @param <T>
     * @return
     */
    public static <T extends SqlSelect> Query createQuery(T hql) {
        return JPA.em().createQuery(hql.toString());
    }

    private static <T extends SqlSelect> Query basicCreateQuery(T hql, ResultTransformer transformer) {
        Query query = JPA.em().createQuery(hql.toString());
        for(int i = 0; i < hql.getParams().size(); i++)
            query.setParameter(i + 1, hql.getParams().get(i));
        HibernateQuery hquery = (HibernateQuery) query;
        hquery.getHibernateQuery().setResultTransformer(transformer);
        return query;
    }
}
