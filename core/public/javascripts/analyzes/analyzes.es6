$(() => {

  $("#click_param").on("change", "[name=p\\.market]", function () {
    ajaxFreshActiveTableTab();
  }).on("click", ".btn:contains(Excel)", function (e) {
    e.preventDefault();
    window.location.href = '/Excels/analyzes?' + $('#click_param').serialize();
  });

  $("#below_tabContent").on("ajaxFresh", "#sid,#sku", function () {
    let $data_table = $("#below_tabContent");
    let $div = $(this);
    $("#postType").val($div.attr("id"));
    LoadMask.mask($data_table);
    $div.load("/Analyzes/analyzes", $('#click_param').serialize(), function (r) {
      $div.find('table').dataTable({
        "sDom": "<'row-fluid'<'col-sm-3'l><'col-sm-9'f>r>t<'row-fluid'<'col-sm-6'i><'col-sm-6'p>>",
        "sPaginationType": "full_numbers",
        "iDisplayLength": 50,
        "aaSorting": [[16, "desc"]],
        "scrollX": true,
        "columnDefs": paramWidth($div.attr("id"))
      });
      LoadMask.unmask($data_table)
    });
  }).on("click", ".pagination a[page]", function (e) {
    e.preventDefault();
    let $a = $(this);
    $('#postPage').val($a.attr('page'));
    ajaxFreshActiveTableTab();
  }).on("change", ".pagination select", function (e) {
    e.preventDefault();
    $('#postPage').val($(this).val());
    ajaxFreshActiveTableTab();
  }).on('click', 'th[orderby]', function (e) {
    //列排序事件
    let $td = $(this);
    $('#postOrderBy').val($td.attr('orderby'));
    let $desc = $('#postDesc');
    if ($td.hasClass('sortable')) {
      $desc.val($desc.val() === "true" ? 'false' : 'true');
    } else {
      $desc.val(true);
    }
    ajaxFreshActiveTableTab()
  });

  $("#sid").on('change', 'select[name=sellingCycle]', function (e) {
    let $select = $(this);
    if ($select.val() !== '') {
      $.post("/sellings/changeSellingCycle", {
        sellingId: $select.data('sellingid'),
        cycle: $select.val()
      },
      function (r) {
        if (r.flag) {
          noty({
            text: 'Selling' + r.message + '生命周期修改为 ' + $select.find("option:selected").text() + '!',
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

  function ajaxFreshActiveTableTab () {
    let type = $("#below_tab li.active a").attr("href");
    $(type).trigger("ajaxFresh");
  }

  //Tab 切换添加事件 bootstrap  shown 事件：点击后触发，ajaxFreshAcitveTableTab()不然会得到旧的TYPE
  $("a[data-toggle='tab']").on('shown.bs.tab', function (e) {
    $('#postPage').val(1);
    ajaxFreshActiveTableTab();
  });

  ajaxFreshActiveTableTab();

});

function paramWidth (type) {
  if (type === "sid") {
    return [
      {
        "width": "150px",
        "targets": [0]
      },
      {
        "width": "100px",
        "targets": [1]
      },
      {
        "width": "28px",
        "targets": [2]
      },
      {
        "width": "28px",
        "targets": [3]
      },
      {
        "width": "28px",
        "targets": [4]
      },
      {
        "width": "28px",
        "targets": [5]
      },
      {
        "width": "28px",
        "targets": [6]
      },
      {
        "width": "28px",
        "targets": [7]
      },
      {
        "width": "28px",
        "targets": [8]
      },
      {
        "width": "28px",
        "targets": [9]
      },
      {
        "width": "28px",
        "targets": [10]
      },
      {
        "width": "15px",
        "targets": [11]
      },
      {
        "width": "15px",
        "targets": [12]
      },
      {
        "width": "15px",
        "targets": [13]
      },
      {
        "width": "15px",
        "targets": [14]
      },
      {
        "width": "15px",
        "targets": [15]
      },
      {
        "width": "15px",
        "targets": [16]
      },
      {
        "width": "15px",
        "targets": [17]
      },
      {
        "width": "15px",
        "targets": [18]
      },
      {
        "width": "15px",
        "targets": [19]
      },
      {
        "width": "15px",
        "targets": [20]
      },
      {
        "width": "35px",
        "targets": [21]
      },
      {
        "width": "35px",
        "targets": [22]
      },
      {
        "width": "15px",
        "targets": [23]
      },
      {
        "width": "45px",
        "targets": [24]
      },
      {
        "width": "30px",
        "targets": [25]
      }
    ]
  } else {
    return [
      {
        "width": "250px",
        "targets": [0]
      },
      {
        "width": "45px",
        "targets": [1]
      },
      {
        "width": "45px",
        "targets": [2]
      },
      {
        "width": "45px",
        "targets": [3]
      },
      {
        "width": "45px",
        "targets": [4]
      },
      {
        "width": "45px",
        "targets": [5]
      },
      {
        "width": "45px",
        "targets": [6]
      },
      {
        "width": "45px",
        "targets": [7]
      },
      {
        "width": "45px",
        "targets": [8]
      },
      {
        "width": "80px",
        "targets": [9]
      },
      {
        "width": "45px",
        "targets": [10]
      },
      {
        "width": "45px",
        "targets": [11]
      },
      {
        "width": "45px",
        "targets": [12]
      },
      {
        "width": "45px",
        "targets": [13]
      },
      {
        "width": "45px",
        "targets": [14]
      },
      {
        "width": "45px",
        "targets": [15]
      },
      {
        "width": "45px",
        "targets": [16]
      },
      {
        "width": "45px",
        "targets": [17]
      },
      {
        "width": "45px",
        "targets": [18]
      }
    ]
  }
}