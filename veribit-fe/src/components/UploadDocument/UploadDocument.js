import React, { PureComponent } from 'react';
import uploadIcon from 'assets/img/upload.png'

class UploadDocument extends PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      selectedFile: '',
      imageBase64: ''
    }
  }

  fileChangedHandler = (event) => {
    if(event.target.files.length !== 0) {
      this.setState(...this.state, {selectedFile: event.target.files[0]})
      var _ = this;
      var reader = new FileReader();
      var file = event.target.files[0];
      reader.onload = function(upload) {
        _.setState(..._.state, {imageBase64: upload.target.result});
      };
      reader.readAsDataURL(file);
      setTimeout(function() {
        _.props.onChooseImage(_.state.imageBase64);
      }, 1000);
    }
  }

  chooseFile = () => {
    this.inputElement.click();
  }

  render() {
    return (
      <div className="upload">
        <input type="file" ref={input => this.inputElement = input} onChange={this.fileChangedHandler}/>
        <img alt="true" src={uploadIcon} className="upload_icon" onClick={this.chooseFile}/>
        <div className="upload_choose_title">
          {
            this.state.selectedFile ? <p className="upload_file_name">{this.state.selectedFile.name}</p>: 'Choose a document'
          }
        </div>
      </div>
    );
  }
}

export default UploadDocument;
