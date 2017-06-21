/**
 * Created by licco on 2016/11/17.
 */
const attrsFormat = {
  'qty': "实际交货数量",
  "handType": '交货不足处理方式',
  "result": '质检结果',
  "way": '质检不合格处理方式',
  "qualifiedQty": '合格数',
  "unqualifiedQty": '不良品数',
  "inboundQty": '实际入库数',
  "target": '目标仓库'
};

$(() => {
  $('#unit_table').on('change', 'td>:input[name$=qty]', function () {
     let $input = $(this);
     let id = $(this).parents('tr').find('input[name$=pids]').val();
     let attr = $input.attr('name');
     let value = $input.val();

     //实际交货数量
     if (attr == 'qty' && $(this).val() < 0) {
       noty({
         text: '收货数量不能小于0!',
         type: 'error'
       });
       $(this).val(0);
       return;
     }

     if (attr == 'qty' && $(this).val() == $(this).data('qty')) {
       $(this).parent('td').next().find('select').hide();
       $(this).attr("style", "width:35px;");
     } else if (attr == 'qty' && $(this).val() != $(this).data('qty')) {
       if ($(this).val() / $(this).data('qty') < 0.9 && $(this).parent('td').next().find('select').val() == 'Delivery') {
         $(this).attr("style", "width:35px;background-color:yellow;");
       }
       $(this).parent('td').next().find('select').show();
     }


     if ($(this).val()) {
       $.post('/MaterialPlans/updateUnit', {
         id: id,
         attr: attr,
         value: value
       }, (r) => {
         if (r) {
           const msg = attrsFormat[attr];
           noty({
             text: '更新' + msg + '成功!',
             type: 'success'
           });
         } else {
           noty({
             text: r.message,
             type: 'error'
           });
         }
       });
     }
   });

//点击明细修改按钮，显示弹出框,并初始化明细数据
  $("#unit_table a[name='unitUpdateBtn']").click(function (e) {
    let id = $(this).attr("uid");
    //赋值
    $("#unit_id").val(id);
    $("#bom_modal").modal('show');
  });


//签收异常js处理
$('#submitUpdateBtn').click(function (e) {
  e.stopPropagation();
  let $btn = $(this);
  let receiptQty = $("#unit_receiptQty").val();
  let $aobj =   $("#qs_"+$("#unit_id").val());
  if (receiptQty == null || receiptQty == undefined || receiptQty == '' || isNaN(receiptQty)) {
    alert("请输入数字");
    $("#unit_receiptQty").focus();
    return false;
  }else{
    $.post('/MaterialPlans/updateMaterialPlanUnit', $("#updateUnit_form").serialize(), (r) =>  {
      if (r) {
               $aobj.parent("td").text(receiptQty);
               $aobj.remove();
               $('#bom_modal').modal('hide');
               noty({
                    text: '更新成功!',
                    type: 'success'
                  });
            } else {
                noty({
                  text: r.message,
                  type: 'error'
                });
            }
    });
  }
  });

  

});

