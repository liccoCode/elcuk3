/**
 * Created by licco on 2016/11/14.
 */
$(() => {
  $("#createInboundBtn,#createOutboundBtn,#createMachiningInboundBtn,#createRefundBtn").click(function(e) {
    e.stopPropagation();
    let $btn = $(this);
    if ($("input[name='pids']:checked").length == 0) {
      noty({
        text: '请选择需要操作的采购单元',
        type: 'error'
      });
      return false;
    } else if (validProNameAndCooperName($btn)) {
      $.post('/Inbounds/createValidate', $("#create_deliveryment").serialize() + "&type=" + $btn.attr("id"), r => {
        if (r.flag) {
          $("#create_deliveryment").attr("action", $btn.data("url")).submit();
        } else {
          noty({
            text: r.message,
            type: 'error'
          });
        }
      });
    }
  });

  function validProNameAndCooperName ($btn) {
    let firstProjectName = $("input[name='pids']:checked").first().attr("project");
    let firstCooper = $("input[name='pids']:checked").first().attr("cooperName");
    let firstStage = $("input[name='pids']:checked").first().attr("stage");
    let i = 0;
    let j = 0;
    $("input[name='pids']:checked").each(function() {
      if ($(this).attr("project") != firstProjectName) {
        i++;
      }
      if ($(this).attr("cooperName") != firstCooper) {
        j++;
      }
    });
    if (i > 0 || j > 0) {
      noty({
        text: '请选择【供应商】【项目名称】一致的采购计划！',
        type: 'error'
      });
      return false;
    }
    if ($btn.attr("id") == 'createOutboundBtn') {
      let firstWhouse = $("input[name='pids']:checked").first().attr("whouse");
      let o = 0;
      if ($(this).attr("whouse") != firstWhouse) {
        i++;
      }
      if (o > 0) {
        noty({
          text: '请选择【目的国家】一致的采购计划！',
          type: 'error'
        });
        return false;
      }
    }

    if ($btn.attr("id") == 'createRefundBtn') {
      i = 0;
      if ($(this).attr("stage") != firstStage) {
        i++;
      }
      if (i > 0) {
        noty({
          text: '请选择相同【阶段】的采购单元',
          type: 'error'
        });
        return false;
      }
    }
    return true;
  }

  $("#stage").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '状态',
    maxHeight: 200,
    includeSelectAllOption: true
  });

});

