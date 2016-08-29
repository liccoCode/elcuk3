package models.view.dto;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 15/10/29
 * Time: 4:50 PM
 */
public class DeclareDTO {

    public String soldTo;

    public String address;

    public String attn;

    public String tel;

    public String email;

    public String vatNo;

    public String buyerEORINO;

    public String buyerVATNO;

    public static DeclareDTO changeCounty(String country) {
        DeclareDTO dto = new DeclareDTO();
        dto.attn = "Yeping Xie";
        dto.email = "Jason@easyacceu.com";
        dto.buyerEORINO = "GB117317336000";
        dto.buyerVATNO = "GB117317336";
        if(country.equals("DE")) {
            dto.soldTo = "Sold to : TUGGLE ELECTRONIC COMMERCE CO.,LTD ";
            dto.address = "DHL Solution Fashion GmbH c/o Amazon FC WRO2 Am Wald 1 Oranienburg, Brandenburg 16515 Germany";
            dto.vatNo = "DE 292695920";
            dto.tel = "+4915211641438";
            dto.buyerVATNO = "DE 292695920";
        } else if(country.equals("FR")) {
            dto.soldTo = "Sold to : SERF Europefides SAS";
            dto.address = "5.rue Denis Poisson 75017 Paris";
            dto.vatNo = "FR63328387212";
            dto.tel = "+33 605717467";
            dto.buyerVATNO = "FR63328387212";
        } else if(country.equals("GB")) {
            dto.soldTo = "Edeer Network Technology CO., LTD";
            dto.address = "53A Woodgrange Road London E7 0EL United Kingdom";
            dto.vatNo = "GB117317336";
            dto.tel = "00447466355519";
        } else if(country.equals("IT")) {
            dto.soldTo = "ACIT S.R.L";
            dto.address = "Via Scarlatti 30,20124 Milano(MI),ITALY";
            dto.vatNo = "IT07289420965";
            dto.tel = "+390707040008";
            dto.buyerVATNO = "IT07289420965";
        } else {

        }
        return dto;
    }


}
