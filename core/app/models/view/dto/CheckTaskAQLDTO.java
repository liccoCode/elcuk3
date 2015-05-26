package models.view.dto;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-5-25
 * Time: PM3:31
 */
public class CheckTaskAQLDTO {
    /**
     * 不良描述
     */
    @Expose
    public String badDesc;

    /**
     * 检验结果
     */
    @Expose
    public List<String> inspectionResult;
}
