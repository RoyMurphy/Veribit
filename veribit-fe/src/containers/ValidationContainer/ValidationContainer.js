import React, { PureComponent } from 'react'; 
import { bindActionCreators } from 'redux';
import { compose } from 'recompose';
import { Icon, Row, Col, Button, Layout } from 'antd';
import { connectAuth, connectValidation, authActionCreators, validationActionCreators } from 'core';
import { promisify } from '../../utilities';
import DocumentSelect from '../../components/DocumentSelect/DocumentSelect';
import DropdownSelect from '../../components/DropdownSelect/DropdownSelect';
import logo from 'assets/img/logo.png';

const { Content, Header } = Layout;

class ValidationContainer extends PureComponent {

  constructor(props) {
    super(props);
    this.state = {
      countries: [],
      docType: 'PASSPORT',
      country: ''
    }
  }

  componentDidMount() {
    promisify(this.props.getCountries, { })
      .then((countries) => {
        this.setState(...this.state, {countries: countries});
      })
      .catch(e => console.log(e));
  }

  showUploadDocPage = () => {
    if (this.props.user) {
      promisify(this.props.updateUser, {
        token: this.props.user.token,
        residenceCountry: this.state.country,
        docType: this.state.docType
      })
        .then((profile) => {
          this.props.history.push('/upload'); 
        })
        .catch(e => console.log(e));
    }
  }
  
  onSelectDoc = (docType) => {
    this.setState(...this.state, {docType: docType});
  }

  onSelectCountry = (country) => {
    this.setState(...this.state, {country: country});
  }
  
  back = () => {
    if (this.props.user)
      this.props.history.push(`/signin/${this.props.user.token}`);
  }

  render () {
    return (
      <div className="block">
        <Layout>
          <Header className="header">
            <div onClick={this.back}>
              <Icon style={{ fontSize: 16 }} type="arrow-left"/> <span>BACK</span>
            </div>
          </Header>
          <Layout>
            <Content className="main">
              <Row className="validation_logo_area">
                <Col span={14} offset={5}>
                  <img alt="true" src={logo} className="logo"/>
                </Col>
              </Row>
              {/* <Row className="validation_logo_area">
                <Col span={12} className="title_area">
                  <Row className="row_title"><Col><span  className="logo_title">NO REST</span></Col></Row>
                  <Row className="row_title"><Col><span className="logo_title">LABS</span></Col></Row>
                </Col>
              </Row> */}
              <Row  className="validation_title_area">
                <Col span={12} offset={6}>
                    <span className="validation_choose_title">Choose&ensp;A&ensp;Document</span>
                </Col>
              </Row>  
              <Row className="document_area">
                <Col offset={4} span={16}>
                    <DocumentSelect onSelectDoc={(docType) => this.onSelectDoc(docType)} />
                </Col>
              </Row>
              <Row className="document_type_area">
                <Col className="document_type_title" offset={4} span={7}>
                    <span>Passport</span>
                </Col>
                <Col className="document_type_title" offset={2} span={7}>
                    <span>ID Card</span>
                </Col>
              </Row>
              <Row className="document_region_area">
                <Col offset={4} span={16}>
                  <DropdownSelect options={this.state.countries} onSelectCountry={(country) => this.onSelectCountry(country)} className="document_region_select" placeholder="Region"/>
                </Col>
              </Row>
              <Row>
                <Col offset={4} span={16}>
                  <Button className={this.state.country === '' ? "continue_btn" : "continue_enable_btn"} disabled={this.state.country === '' ? true : false} onClick={this.showUploadDocPage}>NEXT</Button>
                </Col>
              </Row>
            </Content>
          </Layout>
        </Layout>
      </div>
    );
  }  
}

const mapStateToProps = ({auth}) => ({
  user: auth.user
});

const mapDisptachToProps = (dispatch) => {
  const {
    getCountries
  } = validationActionCreators;

  const {
    updateUser
  } = authActionCreators

  return bindActionCreators({
    getCountries,
    updateUser
  }, dispatch);
}

export default compose(
  connectValidation(undefined, mapDisptachToProps),
  connectAuth(mapStateToProps, mapDisptachToProps),
)(ValidationContainer);