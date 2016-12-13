/**
 * Created by licco on 2016/12/13.
 */
$(() => {

  $("input[name='editBoxInfo']").click(function(e) {
    e.stopPropagation();
    $("#fba_carton_contents_modal").modal('show');
    let id = $(this).data("id");
    $("#refresh_div").load("/Refunds/refreshFbaCartonContentsByIds", {id: id});
  });

  $("#submitBoxInfoBtn").click(function(e) {
    e.stopPropagation();
    let action = $(this).data('action');
    let form = $("<form method='post' action='#{action}'></form>")
    form = form.append($("#box_info_table").clone())
    $.post('/Refunds/updateBoxInfo', form.serialize(), function(re) {
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
