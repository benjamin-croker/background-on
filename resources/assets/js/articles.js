/** @jsx React.DOM */

var BackgroundArticle = React.createClass({
  render: function() {
    return (
      <div className="backgroundArticle">
        <div className="row">
          <div className="col-lg-8 col-md-7 col-sm-6">
            <h4>
              <a href={this.props.url}>{this.props.title}</a>
            </h4>
            <p>{this.props.abstract}</p>
          </div>
        </div>
      </div>  
    );
  }
})

var MainArticle = React.createClass({
  render: function () {
    backgroundNodes = this.props.related.map(function (article) {
      return (
        <BackgroundArticle url={article.url}
                           title={article.title}
                           abstract={article.abstract} />
      );
    })
    return (
      <div className="mainArticle col-lg-8 col-md-7 col-sm-6 well">
          <div className="page-header">
            <h3>
              <a href={this.props.url}>{this.props.title}</a>
            </h3>
          </div>
        <div className="row">
          <div className="col-lg-8 col-md-7 col-sm-6">
            <p className="lead">{this.props.abstract}</p>
          </div>
        </div>
        {backgroundNodes}
      </div>
    );
  }
});

var ArticleList = React.createClass({
  render: function() {
    var articleList = this.props.data.map(function (article) {
      return (
        <MainArticle url={article.url}
                     title={article.title}
                     abstract={article.abstract}
                     related={article.related} />
      );
    });
    return (
      <div className="articleList">
        {articleList}
      </div>
    );
  }
});

var ArticleBox = React.createClass({
  getInitialState: function() {
    return {data: []};
  },
  componentDidMount: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render: function() {
    return (
      <div className="articleBox">
        <ArticleList data={this.state.data} /> 
      </div>
    );
  }
});

React.renderComponent(
  <ArticleBox url="snapshot" />,
  document.getElementById('content')
);