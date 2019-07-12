import React from 'react';
import './App.css';

import { CountClicks, Howdy, Welcome, ShowRandomNumber, PrintDatascript} from 'shadow-cljs/acme.frontend.core';

const DisplayWelcome = Welcome();
const NewCounter = CountClicks();
const ShowAlert = Howdy();
const RandomNumber = ShowRandomNumber();
const PrintData = PrintDatascript();

function App() {
  return (
    <div className="App">
      
      <DisplayWelcome />
      <NewCounter title={"Clicks: "}/>
      <ShowAlert />
      <br/>
      <br/>
      <RandomNumber title={"Display number in Positive"} func={"+"}/>
      <RandomNumber title={"Display number in Negative"} func={"-"}/>
      <br />
      <PrintData />
    </div>
  );
}

export default App;
