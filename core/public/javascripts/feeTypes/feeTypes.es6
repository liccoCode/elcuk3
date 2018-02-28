$(() => {

  $("button.delete").click(function () {
    if (confirm('确认删除这个费用类型吗?!')) {
      let self = $(this);
      let name = self.parents('tr').find("td:eq(0)").text();
      LoadMask.mask();
      $.post("/feetype/" + name + "/delete", function (r) {
        if (r.flag) {
          self.parents('tr').remove();
          alert("删除 FeeType:" + r.message);
        } else {
          alert("删除 FeeType 失败:" + r.message);
        }
        LoadMask.unmask();
      });
    }
  });

  $("#show_modal").click(() => {
    $("#create_modal").modal('show');
  });

  $("#submitCreateBtn").click(() => {
    $("#create_form").submit();
  });

  $("button[name='update_btn']").click(function () {
    $("#update_form input[name='ft.name']").val($(this).data("name"));
    $("#update_form input[name='ft.nickName']").val($(this).data("nick"));
    $("#update_form input[name='ft.memo']").val($(this).data("demo"));
    $("#update_form input[name='ft.shortcut']").val($(this).data("short"));
    $("#update_modal").modal('show');
  });

  $("#submitUpdateBtn").click(function () {
    $("#update_form").submit();
  });

});