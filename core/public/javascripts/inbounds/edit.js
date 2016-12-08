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

    if (attr == 'qualifiedQty') {
      let qty = $(this).data('qty');
      $(this).parent('td').next().find('input').attr("value", qty - value);
    }

    if (attr == 'unqualifiedQty') {
      let qty = $(this).data('qty');
      $(this).parent('td').prev().find('input').attr("value", qty - value);
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

  $("input[name='editBoxInfo']").click(function(e) {
    e.stopPropagation();
    $("#fba_carton_contents_modal").modal('show');
    let id = $(this).data("id");
    $("#refresh_div").load("/Inbounds/refreshFbaCartonContentsByIds", {id: id}, function(r) {
      $("#submitBoxInfoBtn").click(() => {
        let action = $(this).data('action');
        let form = $("<form method='post' action='#{action}'></form>")
        form = form.append($("#box_info_table").clone())
        $.post('/Inbounds/updateBoxInfo', form.serialize(), function(re) {
          if (re) {
            $("#fba_carton_contents_modal").modal('hide');
            noty({
              text: '更新包装信息成功!',
              type: 'success'
            });
          } else {
            noty({
              text: r.message,
              type: 'error'
            });
          }
        });
      });
    });
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



