package models.whouse;

import com.google.gson.annotations.Expose;
import helper.Dates;
import models.User;
import models.market.Account;
import models.market.M;
import models.procure.Cooperator;
import models.procure.Shipment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 不同的仓库的抽象
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:06
 */
@Entity
@DynamicUpdate
public class Whouse extends Model {

    /**
     * 如果这个 Whouse 为 FBA 的, 那么则一定需要绑定一个 Account, 只有这样才能从 Account 获取必要的信息到 Amazon FBA 下载
     * FBA Inventory 库存信息
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Account account;

    public enum T {
        FBA {
            @Override
            public String label() {
                return "FBA 仓库";
            }
        },
        SELF {
            @Override
            public String label() {
                return "自有仓库";
            }
        },
        FORWARD {
            @Override
            public String label() {
                return "货代仓库";
            }
        };

        public abstract String label();
    }


    @Required
    @Column(nullable = false, unique = true)
    @Expose
    public String name;

    @Column(nullable = false)
    @Expose
    public String address;

    @Column(nullable = false)
    @Expose
    public String city;

    @Column(nullable = false)
    @Expose
    public String province;

    @Column(nullable = false)
    @Expose
    public String postalCode;

    @Column(nullable = false)
    @Expose
    public String country;

    @Column(nullable = false)
    @Expose
    @Required
    public T type;

    @Lob
    @Expose
    public String memo;

    /**
     * 容量的提示字符串
     */
    @Lob
    public String capaticyContent = "";

    /**
     * 仓库类型为货代时的合作伙伴(货代)
     */
    @ManyToOne
    public Cooperator cooperator;

    /**
     * 货代仓库的质检员
     */
    @ManyToOne
    public User user;

    /**
     * 运输方式是否为海运
     */
    @Expose
    public boolean isSEA = false;

    /**
     * 是否为空运
     */
    @Expose
    public boolean isAIR = false;

    /**
     * 是否为快递
     */
    @Expose
    public boolean isEXPRESS = false;

    @OneToMany(mappedBy = "whouse", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
            CascadeType.REFRESH}, fetch = FetchType.LAZY)
    public List<WhouseItem> items = new ArrayList<>();

    public void validate() {
        if(this.type == null) return;
        switch(this.type) {
            case FBA:
                if(this.account == null) {
                    Validation.addError("", "wh.fba.account");
                }
                break;
            case FORWARD:
                if(this.cooperator == null) {
                    Validation.addError("", "货代不能为空");
                }
                if(!this.isAIR && !this.isEXPRESS && !this.isSEA) {
                    Validation.addError("", "运输方式不能为空");
                }
                this.exist();
                break;
            default:
                break;
        }
    }

    public StringBuilder buildStringHead() {
        StringBuilder sbd = new StringBuilder();
        sbd.append("cooperator_id = " + this.cooperator.id + "");
        if(this.id != null) {
            //update 的时候查询是否存在时需要将当前对象排除掉
            sbd.append(" AND id != " + this.id + "");
        }
        return sbd;
    }

    /**
     * 判断当前货代该运输方式是否已经被使用
     * <p/>
     * 由于运输方式可以多选，所以需要单独去判断每一个被选择的运输方式是否已经被使用
     *
     * @return
     */
    public void exist() {
        StringBuilder sbd = buildStringHead();
        List<Object> params = new ArrayList<>();
        if(this.isAIR) {
            sbd.append(" AND isAIR = ?");
            params.add(this.isAIR);
            if(Whouse.count(sbd.toString(), params.toArray()) > 0) {
                Validation.addError("", String.format("货代：%s 空运仓库已经存在", this.cooperator.name));
            }
        }
        if(this.isSEA) {
            sbd = buildStringHead();
            params.clear();
            sbd.append(" AND isSEA = ?");
            params.add(this.isSEA);
            long count = Whouse.count(sbd.toString(), params.toArray());
            if(count > 0) {
                Validation.addError("", String.format("货代：%s 海运仓库已经存在", this.cooperator.name));
            }
        }
        if(this.isEXPRESS) {
            sbd = buildStringHead();
            params.clear();
            sbd.append(" AND isEXPRESS = ?");
            params.add(this.isEXPRESS);
            if(Whouse.count(sbd.toString(), params.toArray()) > 0) {
                Validation.addError("", String.format("货代：%s 快递仓库已经存在", this.cooperator.name));
            }
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.address = String.format("%s %s %s %s", this.country, this.province, this.city,
                this.postalCode);
    }


    public void setName(String name) {
        this.name = name;
        if(this.name != null) this.name = this.name.toUpperCase();
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Whouse whouse = (Whouse) o;

        if(name != null ? !name.equals(whouse.name) : whouse.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public String name() {
        return String.format("%s: %s", this.type, this.name);
    }

    public static List<Whouse> findByType(T type) {
        return Whouse.find("type=?", type).fetch();
    }

    public static List<Whouse> findByAccount(Account account) {
        return Whouse.find("account=? and type=?", account, T.FBA).fetch();
    }

    /**
     * 根据星期判断shipmentType来处理运往某仓库的Shipment
     *
     * @param planShipments 用于判断的已经存在的运输单
     * @return 暂时无返回
     */
    public void checkWhouseNewShipment(List<Shipment> planShipments) {
        /**
         * 1. 处理 60 天内的运输单;
         * 2. 规则:
         *  空运: 每周三;
         *  海运: DE/US 周一, IT 周五, UK 周五
         */
        DateTime now = new DateTime(Dates.morning(new Date()));
        for(int i = 0; i < 60; i++) {
            DateTime nextBeginDate = now.plusDays(i);
            M type = this.account.type;

            Object exist = CollectionUtils.find(planShipments, new PlanDateEqual(nextBeginDate.toDate(), type));
            if(exist != null) continue;

            if(nextBeginDate.getDayOfWeek() == 1) {
                if(Arrays.asList(M.AMAZON_DE, M.AMAZON_US).contains(type)) {
                    Shipment.checkNotExistAndCreate(nextBeginDate.toDate(), Shipment.T.SEA, this);
                }
            } else if(nextBeginDate.getDayOfWeek() == 2) {
                //Nothing
            } else if(nextBeginDate.getDayOfWeek() == 3) {
                if(Arrays.asList(M.AMAZON_DE, M.AMAZON_FR, M.AMAZON_UK, M.AMAZON_US, M.AMAZON_CA, M.AMAZON_IT,
                        M.AMAZON_JP).contains(type)) {
                    Shipment.checkNotExistAndCreate(nextBeginDate.toDate(), Shipment.T.AIR, this);
                }
            } else if(nextBeginDate.getDayOfWeek() == 5) {
                if(Arrays.asList(M.AMAZON_UK, M.AMAZON_IT).contains(type)) {
                    Shipment.checkNotExistAndCreate(nextBeginDate.toDate(), Shipment.T.SEA, this);
                }
                //else
                //throw new FastRuntimeException("还不支持向 " + type.name() + " 仓库创建运输单");
            }
        }
    }

    class PlanDateEqual implements Predicate {
        // 期待的日期
        private Date date;
        private M market;

        PlanDateEqual(Date date, M market) {
            this.date = date;
            this.market = market;
        }

        @Override
        public boolean evaluate(Object o) {
            Shipment ship = (Shipment) o;
            return Dates.morning(ship.dates.planBeginDate).equals(Dates.morning(this.date))
                    && ship.whouse.account.type == this.market;
        }
    }

    /**
     * 根据运输商与运输方式来查找仓库
     *
     * @return
     */
    public static Whouse findByCooperatorAndShipType(Cooperator cooperator, Shipment.T shiptype) {
        StringBuilder sbd = new StringBuilder("cooperator=?");
        switch(shiptype) {
            case SEA:
                sbd.append(" AND isSEA=true");
                break;
            case EXPRESS:
                sbd.append(" AND isEXPRESS=true");
                break;
            case AIR:
                sbd.append(" AND isAIR=true");
                break;
            default:
                throw new FastRuntimeException("不支持的 ShipType");
        }
        return Whouse.find(sbd.toString(), cooperator).first();
    }

    /**
     * 自有仓
     *
     * @return
     */
    public static List<Whouse> selfWhouses() {
        return selfWhouses(true);
    }

    /**
     * 自有仓
     *
     * @param includeDefective
     * @return
     */
    public static List<Whouse> selfWhouses(boolean includeDefective) {
        String sql = "type=?";
        if(!includeDefective) sql += " AND name NOT LIKE '%不良品仓%'";
        return Whouse.find(sql, T.SELF).fetch();
    }

    /**
     * 不良品仓库
     *
     * @return
     */
    public static Whouse defectiveWhouse() {
        return Whouse.find("type=? AND name Like ?", T.SELF, "%不良品仓%").first();
    }

    public static Whouse autoMatching(InboundUnit unit) {
        StringBuffer sql = new StringBuffer("type=?  ");
        if(unit.unit.shipType == null) {
            return null;
        }
        switch(unit.unit.shipType) {
            case AIR:
                sql.append("AND isAIR=true");
                break;
            case EXPRESS:
                sql.append("AND isEXPRESS=true");
                break;
            case SEA:
                sql.append("AND isSEA=true");
                break;

        }
        sql.append(" AND country = ? ");
        return Whouse.find(sql.toString(), T.SELF, unit.unit.selling.market.country()).first();
    }

}
