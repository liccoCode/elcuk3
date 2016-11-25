/**
 * Created by licco on 2016/11/14.
 */
$(document).ready(function(){
    $("#createInbound").click(function(e){
        e.stopPropagation();
        if($("input[name='pids']:checked").length == 0){
            noty({text: '请选择需要下载的采购单元', type: 'error'});
            return false
        }else{
            $("#create_deliveryment").attr("action", $("#createInbound").data("url")).submit();
        }
    });

});

