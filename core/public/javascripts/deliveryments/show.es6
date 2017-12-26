$(() => {

  $(document).on('click', "#delete_unit_btn,  #downloadProcureunitsOrder", function () {
    let $btn = $(this);
    let checkboxs = $btn.parents('form').find('input[name="pids"]');
    let msg = "确认 " + $btn.text().trim() + " ?";
    if ($btn.attr('id') === 'delunit_form_submit' && $btn.data('url').indexOf('deliverplans') !== -1
    && checkboxs.size() !== 0 && checkboxs.size() === checkboxs.filter(':checked').size()) {
      msg += " (注: 移除所有的采购单元后默认会删除掉当前出货单)"
    }
    if (confirm(msg)) {
      $("#bulkpost").attr('action', $btn.data('url')).submit()
    }

  });

});