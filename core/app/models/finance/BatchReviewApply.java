package models.finance;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.User;
import models.procure.Cooperator;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/7/29
 * Time: 下午1:52
 */
@Entity
public class BatchReviewApply extends GenericModel {

    private static final long serialVersionUID = -7484838929295487270L;

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    public String name;


    public enum W {
        PREPAY {
            @Override
            public String label() {
                return "预付";
            }
        },
        MONTHLY {
            @Override
            public String label() {
                return "月结";
            }
        },
        PERIOD {
            @Override
            public String label() {
                return "账期";
            }
        };

        public abstract String label();
    }

    @Enumerated(EnumType.STRING)
    public W way;

    public enum S {
        Pending {
            @Override
            public String label() {
                return "待审核";
            }
        },
        Brand {
            @Override
            public String label() {
                return "品牌部审核中";
            }
        },
        Audit {
            @Override
            public String label() {
                return "审计部审核中";
            }
        },
        Finance {
            @Override
            public String label() {
                return "财务部审核中";
            }
        },
        End {
            @Override
            public String label() {
                return "审核结束";
            }
        };

        public abstract String label();
    }

    @Enumerated(EnumType.STRING)
    public S status;

    @OneToOne
    public Cooperator cooperator;

    @OneToOne
    public User creator;

    public Date createDate;

    public String memo;

    @OneToMany(mappedBy = "batchReviewApply", cascade = {CascadeType.PERSIST})
    public List<Payment> paymentList = new ArrayList<>();

    @OneToMany(mappedBy = "apply", cascade = {CascadeType.PERSIST})
    @OrderBy("createDate desc")
    public List<BatchReviewHandler> handlers = new ArrayList<>();

    public String id() {
        String count = BatchReviewApply.count("cooperator=? ", this.cooperator) + "";
        return String.format("QKSH-%s-%s", this.cooperator.name, count.length() == 1 ? "0" + count : count);
    }

    public F.T3<Float, Float, Float> totalApplyAmount() {
        float currentUSDAmount = 0;
        float currentCNYAmount = 0;
        float currentCurrencyAmount = 0;
        for(Payment payment : this.paymentList) {
            F.T3<Float, Float, Float> t3 = payment.totalFees();
            currentUSDAmount += t3._1;
            currentCNYAmount += t3._2;
            currentCurrencyAmount += t3._2;
        }
        return new F.T3<>(currentUSDAmount, currentCNYAmount, currentCurrencyAmount);
    }

    /**
     * @return
     */
    public boolean showSubmitBtn() {
        User user = Login.current();
        boolean flag = false;
        if(Arrays.asList(S.Pending, S.Brand).contains(this.status) && Objects.equals(user.department, User.D.Brand)) {
            flag = true;
        }
        if(Objects.equals(S.Audit, this.status) && Objects.equals(user.department, User.D.Audit)) {
            flag = true;
        }
        if(Objects.equals(S.Finance, this.status) && Objects.equals(user.department, User.D.Finance)) {
            flag = true;
        }
        return !this.handlers.stream().anyMatch(handler -> Objects.equals(handler.handler, user) && handler.result.name()
                .equals("Agree")) && flag;
    }

    public List<BatchReviewHandler> getHandlers(User.D department) {
        return this.handlers.stream()
                .filter(handler -> handler.handler.department == department && handler.effective)
                .collect(Collectors.toList());
    }

}
