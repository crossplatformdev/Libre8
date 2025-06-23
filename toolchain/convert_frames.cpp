#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <cstdio>      // FILE*
#include <jpeglib.h>   // Librería libjpeg

// Función para leer un archivo JPEG y extraer su contenido en una matriz (vector)
// con 3 canales (R, G, B). Devuelve true si se pudo leer la imagen correctamente.
bool readJPEG(const std::string& filename, 
              std::vector<unsigned char>& imageData, 
              unsigned int& width, 
              unsigned int& height)
{
    // Estructuras necesarias para descompresión en libjpeg
    jpeg_decompress_struct cinfo;
    jpeg_error_mgr jerr;

    // Asocia el manejador de errores
    cinfo.err = jpeg_std_error(&jerr);

    // Inicializa objeto de descompresión
    jpeg_create_decompress(&cinfo);

    // Abre el archivo
    FILE* infile = std::fopen(filename.c_str(), "rb");
    if (!infile) {
        std::cerr << "No se pudo abrir el archivo: " << filename << std::endl;
        return false;
    }

    // Vincula el archivo a la estructura de descompresión
    jpeg_stdio_src(&cinfo, infile);

    // Lee la cabecera JPEG
    jpeg_read_header(&cinfo, TRUE);

    // Fuerza salida en RGB (3 canales)
    cinfo.out_color_space = JCS_RGB;

    // Inicia la descompresión
    jpeg_start_decompress(&cinfo);

    // Obtiene dimensiones
    width  = cinfo.output_width;
    height = cinfo.output_height;
    unsigned int channels = cinfo.output_components; // debería ser 3 (RGB)

    // Reserva espacio para la imagen completa: width * height * 3
    imageData.resize(width * height * channels);

    // Libjpeg procesa la imagen línea por línea
    while (cinfo.output_scanline < cinfo.output_height) {
        // Apuntador a la línea actual en el buffer
        unsigned char* bufferArray[1];
        // Calcula dónde inicia la línea dentro de imageData
        bufferArray[0] = &imageData[(cinfo.output_scanline) * width * channels];

        // Lee una línea
        jpeg_read_scanlines(&cinfo, bufferArray, 1);
    }

    // Finaliza la descompresión
    jpeg_finish_decompress(&cinfo);

    // Libera los recursos de libjpeg
    jpeg_destroy_decompress(&cinfo);

    // Cierra el archivo
    std::fclose(infile);

    return true;
}

int main()
{
    // Abre el archivo de salida donde se escribirán los píxeles de TODAS las imágenes
    std::ofstream outputFile("pixels_output.txt");
    if (!outputFile.is_open()) {
        std::cerr << "Error al crear/abrir el archivo pixels_output.txt" << std::endl;
        return -1;
    }

    // Bucle desde 1176 hasta 2006
    for (int index = 0; index <= 3970; ++index)
    {
        // Construye el nombre del fichero (por ej. "resized-__1176.jpg")
        std::string filename = "__" + std::to_string(index) + ".jpg";

        // Variables para almacenar la imagen
        std::vector<unsigned char> imageData; 
        unsigned int width = 0, height = 0;

        // Lee la imagen JPEG con libjpeg
        if (!readJPEG(filename, imageData, width, height)) {
            // Si falla, muestra error y pasa a la siguiente
            std::cerr << "No se pudo leer la imagen: " << filename << std::endl;
            continue;
        }

        // Verifica que la imagen tenga 72x32 (según requerimiento)
        if (width != 72 || height != 32) {
            std::cerr << "Dimensiones inesperadas en la imagen: " << filename 
                      << " (" << width << "x" << height << ")" << std::endl;
            continue;
        }

        // Recorre todos los píxeles (y, x)
        // Recordar: imageData está en formato (R, G, B) para cada pixel
        for (unsigned int y = 0; y < height; ++y) {
            for (unsigned int x = 0; x < width; ++x) {

                // Índice del primer canal del píxel en imageData
                // Cada píxel ocupa 3 bytes (R, G, B)
                unsigned int indexPixel = (y * width + x) * 3;
                
                // Extrae R, G, B
                unsigned char R = imageData[indexPixel + 0];
                unsigned char G = imageData[indexPixel + 1];
                unsigned char B = imageData[indexPixel + 2];

                // Escribe al archivo de texto:
                // Formato: "nombreImagen x y R G B"
                // Ajusta según prefieras
                outputFile << filename << " "
                           << x << " " << y << " "
                           << static_cast<int>(R) << " "
                           << static_cast<int>(G) << " "
                           << static_cast<int>(B) << "\n";
            }
        }
    }

    // Cierra el archivo de salida
    outputFile.close();
    std::cout << "Proceso completado. Datos guardados en pixels_output.txt" << std::endl;

    return 0;
}

