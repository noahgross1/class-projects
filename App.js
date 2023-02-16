import React from 'react';
import Header from './ui/Header';
import { ThemeProvider } from '@material-ui/styles';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import theme from './ui/Theme';
import Home from './Home';
import Services from './Services';
import Revolution from './Revolution';
import About from './About';
import Contact from './Contact';
import Estimate from './Estimate';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <BrowserRouter>
        <Header />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/services" element={<Services />} />
          <Route path="/customsoftware" element={<Header />} />
          <Route path="/mobileapps" element={<Header />} />
          <Route path="/websites" element={<Header />} />
          <Route path="/revolution" element={<Revolution />} />
          <Route path="/about" element={<About />} />
          <Route path="/contact" element={<Contact />} />
          <Route path="/estimate" element={<Estimate />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
