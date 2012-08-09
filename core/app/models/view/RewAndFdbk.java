package models.view;

/**
 * 给前台使用一个 DTO , 这个里面的数据不需要修改, 只需要读取读取展示即可.
 * User: wyattpan
 * Date: 8/9/12
 * Time: 10:28 AM
 */
public class RewAndFdbk {
    /**
     * 可以是 SKU 或者也可以是 sid
     */
    public String fid;

    /**
     * 负评比例 负评个数/销量
     */
    public Float negtiveRatio;

    /**
     * 销量
     */
    public Float sales;

    /*
   一星到五星的 Review 数量
    */

    public int s1;
    public int s2;
    public int s3;
    public int s4;
    public int s5;

    /**
     * 处理成功的 Review 数量
     */
    public int updateSucc;
}
