/**
 * Created by licco on 2016/11/17.
 */

$(() => {
  $('#confirmReceiveBtn,#confirmQCBtn,#confirmInboundBtn').click(function(e) {
    e.stopPropagation();
    $('#edit_inbound_form').attr('action', $(this).data('url'));
    $('#edit_inbound_form').submit();
  });

  $('#unit_table').on('change', 'td>:input[name]', function() {
    let $input = $(this);
    let id = $(this).parents('tr').find('input[name$=id]').val();
    let attr = $input.attr('name');
    let value = $input.val();

    if (attr == 'qty' && $(this).val() == $(this).data('qty')) {
      $(this).parent('td').next().find('select').hide();
    } else if (attr == 'qty' && $(this).val() != $(this).data('qty')) {
      $(this).parent('td').next().find('select').show();
    }

    if (attr == 'result' && value == 'Unqualified') {
      $(this).parent('td').next().find('select').show();
    } else if (attr == 'result' && value == 'Qualified') {
      $(this).parent('td').next().find('select').hide();
    }

    if ($(this).val()) {
      $.post('/Inbounds/updateUnit', {
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
});

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



