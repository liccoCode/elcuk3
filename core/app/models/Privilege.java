package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限表.
 * 权限是项目初始化的时候初始化好的, 是固定在代码中的, 不需要设置成可增加修改这个权限.
 * <p/>
 * 权限的修改是在修改与删除: 用户所拥有的权限.
 * <p/>
 * User: wyattpan
 * Date: 10/25/12
 * Time: 11:44 AM
 */
@Entity
public class Privilege extends Model {
    /**
     * 将用户的权限缓存起来, 不用每次判断都去 db 取(注:更新权限的时候也需要更新缓存)
     */
    private static final Map<String, Set<Privilege>> PRIVILEGE_CACHE = new ConcurrentHashMap<String, Set<Privilege>>();

    public Privilege() {
    }

    public Privilege(String name, String memo) {
        this.name = name;
        this.memo = memo;
    }

    /**
     * 权限的名字
     */
    @Column(unique = true, nullable = false)
    @Required
    public String name;

    public String memo;

    /**
     * 用户权限的用户
     */
    @ManyToMany(mappedBy = "privileges")
    public List<User> users = new ArrayList<User>();

    @PrePersist
    @PreUpdate
    public void lowerName() {
        this.name = this.name.toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Privilege privilege = (Privilege) o;

        if(name != null ? !name.equals(privilege.name) : privilege.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    /**
     * 初始化系统内的权限
     */
    public static void init() {
        // 拥有权限则跳过初始化
        if(Privilege.count() > 1) return;
        List<Privilege> privileges = new ArrayList<Privilege>();
        // 模块
        privileges.add(new Privilege("market", "市场模块"));
        privileges.add(new Privilege("listings.index", "Listing 页面"));
        privileges.add(new Privilege("listings.trackedlistings", "Tracked Listing 页面"));
        privileges.add(new Privilege("orders.index", "订单 页面"));
        privileges.add(new Privilege("analyzes.index", "销量分析 页面"));
        privileges.add(new Privilege("amazonReviews.index", "Review点击台 页面"));

        privileges.add(new Privilege("support", "售后模块"));
        privileges.add(new Privilege("tickets.index", "Tickets 工作台 页面"));
        privileges.add(new Privilege("ticketanalyzes.index", "Ticket 分析 页面"));

        privileges.add(new Privilege("product", "产品模块"));
        privileges.add(new Privilege("products.index", "产品 页面"));
        privileges.add(new Privilege("whouses.index", "仓库 页面"));
        privileges.add(new Privilege("categorys.show", "产品类别 页面"));
        privileges.add(new Privilege("brands.index", "产品品牌 页面"));
        privileges.add(new Privilege("familys.index", "Family 页面"));

        privileges.add(new Privilege("procure", "采购模块"));
        privileges.add(new Privilege("procures.index", "采购计划 页面"));
        privileges.add(new Privilege("deliveryments.index", "采购单 页面"));
        privileges.add(new Privilege("shipments.index", "运输单 页面"));
        privileges.add(new Privilege("cooperators.index", "合作伙伴 页面"));

        privileges.add(new Privilege("system", "系统模块"));
        privileges.add(new Privilege("jobs.index", "任务管理 页面"));
        privileges.add(new Privilege("servers.index", "服务器管理 页面"));
        privileges.add(new Privilege("accounts.index", "账户管理 页面"));
        privileges.add(new Privilege("users.index", "用户管理 页面"));
        privileges.add(new Privilege("mailsrecords.index","邮件管理 页面"));

        // 首页
        privileges.add(new Privilege("application.categorypercent", "首页销售额数据"));

        // Analyzes 页面
        privileges.add(new Privilege("analyzes.ajaxsales", "查看销售额"));
        privileges.add(new Privilege("analyzes.allskucsv", "下载 SKU CSV 文件"));

        // 采购单页面
        privileges.add(new Privilege("deliveryments.cancel", "取消采购单"));
        privileges.add(new Privilege("procures.dosplitunit", "分拆采购计划"));
        privileges.add(new Privilege("procures.delivery", "采购计划交货"));
        privileges.add(new Privilege("procures.createdeliveryment", "通过采购计划创建采购单"));

        // 运输单页面
        privileges.add(new Privilege("shipments.cancel", "取消运输单"));
        privileges.add(new Privilege("shipments.beginship", "开始运输"));
        privileges.add(new Privilege("shipments.cancelship", "取消运输项目"));
        privileges.add(new Privilege("shipments.ship", "增加运输项目"));
        privileges.add(new Privilege("shipments.deploytoamazon", "创建 FBA"));
        privileges.add(new Privilege("shipments.deployfba", "删除 FBA"));
        privileges.add(new Privilege("shipments.splitshipment", "分拆运输项目"));
        privileges.add(new Privilege("shipments.movefba", "转移 FBA"));

        // Notification
        privileges.add(new Privilege("notifications.notifys", "通知某个用户"));
        privileges.add(new Privilege("notifications.notifysall", "创建所有用户通知"));

        // 财务控制器
        privileges.add(new Privilege("finances", "财务模块"));
        privileges.add(new Privilege("finances.index", "系统内修复"));
        privileges.add(new Privilege("finances.fixfinance", "修复 Amazon 财务数据"));
        privileges.add(new Privilege("finances.addselling", "修复系统内上架失败 Selling"));
        privileges.add(new Privilege("finances.reparseorder", "重新解析订单文件"));

        privileges.add(new Privilege("applys.index", "请款单列表页面"));
        privileges.add(new Privilege("applys.procure", "采购请款单页面"));

        privileges.add(new Privilege("paymenttargets.index", "支付方式列表页面"));
        privileges.add(new Privilege("paymenttargets.save", "创建支付方式"));
        privileges.add(new Privilege("paymenttargets.update", "更新支付方式"));
        privileges.add(new Privilege("paymenttargets.destroy", "删除支付方式"));

        privileges.add(new Privilege("payments.index", "付款单列表页面"));
        privileges.add(new Privilege("payments.show", "付款单查看"));
        privileges.add(new Privilege("payments.paymentunitapproval", "付款单中请款项批准"));
        privileges.add(new Privilege("payments.payforit", "付款单付款"));
        privileges.add(new Privilege("payments.shouldpaidupdate", "付款单应付金额更新"));
        privileges.add(new Privilege("payments.uploads", "付款单凭证上传"));

        privileges.add(new Privilege("deliveryments.deliverymenttoapply", "采购单生成请款单"));

        // ProcureUnits
        privileges.add(new Privilege("procureunits.billingprepay", "采购计划预付款申请"));
        privileges.add(new Privilege("procureunits.billingtailpay", "采购计划尾款申请"));

        // PaymentUnits
        privileges.add(new Privilege("paymentunits.destroy", "删除请款项目"));
        privileges.add(new Privilege("paymentunits.fixvalue", "更新请款项目修正价"));
        privileges.add(new Privilege("paymentunits.deny", "拒绝请款项目"));


        // FeeTypes 控制器
        privileges.add(new Privilege("feetypes", "费用类型"));
        privileges.add(new Privilege("feetypes.index", "费用类型页面"));
        // todo: 到这里都是新的

        // Products 控制器
        privileges.add(new Privilege("products.saleamazonlisting", "Amazon 上架"));

        // Excels 的下载控制
        privileges.add(new Privilege("excels.deliveryment", "生成采购单 Excel"));
        for(Privilege p : privileges) p.save();
    }

    /**
     * 初始化或者获取缓存的权限
     *
     * @param username
     * @return
     */
    public static Set<Privilege> privileges(String username) {
        Set<Privilege> privileges = PRIVILEGE_CACHE.get(username);
        if(privileges == null) {
            PRIVILEGE_CACHE.put(username, /*这里拿一个 Privileges 的备份*/
                    new HashSet<Privilege>(User.findByUserName(username).privileges));
            privileges = PRIVILEGE_CACHE.get(username);
        }
        return privileges;
    }

    /**
     * 更新缓存的权限
     *
     * @param username
     * @param privileges
     */
    public static void updatePrivileges(String username, Set<Privilege> privileges) {
        PRIVILEGE_CACHE.remove(username);
        PRIVILEGE_CACHE.put(username, privileges);
    }

    /**
     * 清理缓存的权限
     *
     * @param user
     */
    public static void clearUserPrivilegesCache(User user) {
        PRIVILEGE_CACHE.remove(user.username);
    }
}
