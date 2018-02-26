$(() => {
  $('#privilege_form').ajaxForm({
    dataType: 'json',
    success: function (r) {
      alert(r.message);
    }
  });

  $(':checkbox').change(function (e) {
    let $o = $(this);
    if ($o.prop('checked')) {
      $o.parents('div').find(":checkbox[value='" + $o.attr('pid') + "']").prop("checked", $o.prop('checked'));
      let pid = $o.parents('div').find(":checkbox[value='" + $o.attr('pid') + "']").attr('pid');
      $o.parents('div').find(":checkbox[value='" + pid + "']").prop("checked", $o.prop('checked'));
    }
    $o.parents('div').find(":checkbox[class='menu" + $o.attr('value') + "']").prop("checked", $o.prop('checked'));
    $o.parents('table').find(":checkbox[class='menu" + $o.attr('value') + "']").trigger('change');
  });

  $("[name='show_user_btn']").click(function () {
    let tr = $(this).parent().parent("tr");
    let id = $(this).data("id");
    let url = $(this).data("url");
    if ($("#div" + id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      showTr(tr, id, url)
    }
  });

  function showTr (tr, id, url) {
    let html = "<tr style='background-color:#F2F2F2'><td colspan='12'>";
    html += "<div id='div" + id + "'></div></td></tr>";
    tr.after(html);
    $("#div" + id).load(url, {roleId: id});
  }

});