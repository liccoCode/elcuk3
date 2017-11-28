$(() => {
  $('#cci').click(() => {
    $.post('/application/clearCache', {}, (r) => {
      if (r.flag) {
        alert('清理首页缓存成功');
      } else {
        alert('清理失败.');
      }
    });
  });

  $.post("/application/perDayOrderNum", function (r) {
    let title = r.title == undefined ? r["series"][0]["name"] : r.title;
    Highcharts.chart('pieChart1', {
      credits: {
        text: "EasyAcc",
        href: ""
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
      },
      title: {
        text: title
      },
      tooltip: {
        pointFormat: '{point.y}/{point.total}: <b>{point.percentage:.1f}%</b>'
      },
      plotOptions: {
        pie: {
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false
          },
          showInLegend: true
        }
      },
      series: r["series"]
    });
  });

  $.post("/application/ajaxUnit", function (r) {
    Highcharts.chart("totalAjaxUnitDiv", {
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      title: {
        text: "All Unit Order"
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
        buttons: [{
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
        type: 'datetime',
        yAxis: {min: 0}
      },
      tooltip: {
        shared: true,
        crosshairs: true,
        xDateFormat: '%Y-%m-%d'
      },
      series: r['series']
    });
  });

  let libColor = 'rgba(240,190,50,0.80)', grnColor = 'rgba(90,200,90,0.80)';

  $.post("/Application/mapJsonReturn", function (r) {
    Highcharts.mapChart('ajaxMap', {
      chart: {
        borderWidth: 0
      },
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      
      colorAxis: {
        dataClasses: [{
          to: 100
        }, {
          from: 100,
          to: 200
        }, {
          from: 200,
          to: 300
        }, {
          from: 300,
          to: 500
        }, {
          from: 500,
          to: 1000
        }, {
          from: 1000,
          to: 2000
        }, {
          from: 2000
        }]
      },

      colors: ['rgba(19,64,117,0.05)', 'rgba(19,64,117,0.2)', 'rgba(19,64,117,0.4)',
        'rgba(19,64,117,0.5)', 'rgba(19,64,117,0.6)', 'rgba(19,64,117,0.8)', 'rgba(19,64,117,1)'],

      title: {
        text: 'order num by country'
      },

      mapNavigation: {
        enabled: true
      },

      legend: {
        title: {
          text: 'order num',
          style: {
            color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
          }
        },
        align: 'left',
        verticalAlign: 'bottom',
        floating: true,
        layout: 'vertical',
        valueDecimals: 0,
        backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || 'rgba(255, 255, 255, 0.85)',
        symbolRadius: 0,
        symbolHeight: 14
      },

      series: [{
        data: r,
        mapData: Highcharts.maps['custom/world'],
        joinBy: ['iso-a2', 'code'],
        animation: true,
        name: 'Order Nums.',
        states: {
          hover: {
            color: '#a4edba'
          }
        },
        tooltip: {
          valueSuffix: ''
        },
        shadow: false
      }]
    });
  });

});
