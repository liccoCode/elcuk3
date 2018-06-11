$(() => {

  // Form 搜索功能
  $("#click_param").on("change", "[name='p.market']", function () {
    ajaxSaleUnitLines();
    ajaxFreshActiveTableTab();
  }).on("click", ".btn:contains(Excel)", function (e) {
    e.preventDefault();
    window.location.href = '/Excels/analyzes?' + $('#click_param').serialize();
  }).on("click", ".btn:contains(搜索)", function (e) {
    e.preventDefault();
    if ($("select[name|='p.categoryId']").val() === '') {
      $("#postVal").val('all');
    } else {
      ajaxSaleUnitLines()
    }
    ajaxFreshActiveTableTab()
  }).on("click", ".btn:contains(Reload)", function (e) {
    e.preventDefault();
    ajaxSaleUnitLines();
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
        "aaSorting": [[17, "desc"]],
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
    ajaxFreshActiveTableTab();
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

  //Tab 切换添加事件 bootstrap  shown 事件：点击后触发，ajaxFreshActiveTableTab()不然会得到旧的TYPE
  $("a[data-toggle='tab']").on('shown.bs.tab', function (e) {
    $('#postPage').val(1);
    ajaxFreshActiveTableTab();
  });

  $("#basic").on('ajaxFreshUnit', '#a_units, #a_turn, #a_ss', function (e, headName, yName, plotEvents, noDataDisplayMessage) {
    let $div = $(this);
    LoadMask.mask($div);
    $.post("/analyzes/" + $div.data('method'), $('#click_param').serialize(), function (r) {
      if (r['series'].length != 0) {
        $div.highcharts('StockChart', {
          credits: {
            text: "EasyAcc",
            href: ''
          },
          title: {
            text: headName
          },
          legend: {
            enabled: true
          },
          navigator: {
            enabled: true
          },
          scrollbar: {
            enabled: false
          },
          rangeSelector: {
            enabled: true,
            button: [{
              type: 'week',
              count: 1,
              text: '1w'
            }, {
              type: 'month',
              count: 1,
              text: '1m'
            }],
            selected: 1
          },
          xAxis: {
            type: 'datetime'
          },
          yAxis: {min: 0},
          plotOptions: {
            series: {
              cursor: "pointer",
              events: plotEvents
            }
          },
          tooltip: {
            shared: true,
            formatter: function () {
              let s = "<b>" + Highcharts.dateFormat('%Y-%m-%d', this.x) + "</b><br>";
              this.points.forEach(function (point) {
                let totalY = point.series.yData.reduce(function (a, b) {
                  return a + b;
                });
                s += "<span style='color:" + point.series.color + "'>" + point.series.name + "</span>:<b>" + point.y + " (" + totalY + ")</b><br>";
              });
              return s;
            },
            crosshairs: true,
            xDateFormat: '%Y-%m-%d'
          },
          series: r['series']
        });
      } else {
        $div.html(noDataDisplayMessage);
      }
      LoadMask.unmask($div);
    });
  });

  function ajaxSaleUnitLines () {
    let categoryId = $("select[name|='p.categoryId']").val();
    let $postVal = $("#postVal");
    let displayStr;
    if (categoryId === '') {
      displayStr = $postVal.val();
    } else {
      displayStr = 'Category:' + categoryId;
      $postVal.val(categoryId);
    }
    let head = "Selling [<span style='color:orange'>" + displayStr + "</span> | " + $('#postType').val().toUpperCase() + "] Unit Order";
    $("#a_units").trigger("ajaxFreshUnit", [head, "Units", {}, '没有数据, 无法绘制曲线...']);
  }

  ajaxFreshActiveTableTab();

  $("a[name='refreshCache']").click(function () {
    let key = $(this).attr("key");
    $.post("/Analyzes/batchDelete", {
      key: key
    },
    function (r) {
      if (r.flag) {
        noty({
          text: '缓存清除成功',
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

function paramWidth (type) {
  if (type === "sid") {
    return [
      {
        "width": "180px",
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
        "width": "28px",
        "targets": [11]
      },
      {
        "width": "28px",
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
        "width": "15px",
        "targets": [21]
      },
      {
        "width": "15px",
        "targets": [22]
      },
      {
        "width": "35px",
        "targets": [23]
      },
      {
        "width": "35px",
        "targets": [24]
      },
      {
        "width": "15px",
        "targets": [25]
      },
      {
        "width": "45px",
        "targets": [26]
      },
      {
        "width": "35px",
        "targets": [27]
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
        "width": "40px",
        "targets": [3]
      },
      {
        "width": "40px",
        "targets": [4]
      },
      {
        "width": "40px",
        "targets": [5]
      },
      {
        "width": "40px",
        "targets": [6]
      },
      {
        "width": "40px",
        "targets": [7]
      }, {
        "width": "40px",
        "targets": [8]
      },
      {
        "width": "40px",
        "targets": [9]
      },
      {
        "width": "60px",
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
        "width": "90px",
        "targets": [13]
      },
      {
        "width": "40px",
        "targets": [14]
      },
      {
        "width": "40px",
        "targets": [15]
      },
      {
        "width": "40px",
        "targets": [16]
      },
      {
        "width": "40px",
        "targets": [17]
      },
      {
        "width": "40px",
        "targets": [18]
      },
      {
        "width": "40px",
        "targets": [19]
      }
    ]
  }
}

