
#ifndef __EXTENSIONS__MODULES__CHANNEL__LETHE__ADAPTER_H
#define __EXTENSIONS__MODULES__CHANNEL__LETHE__ADAPTER_H

#include <utility>
	
namespace Extensions {
	namespace Modules {
        namespace Channel {
            namespace Lethe {
                template <class InputStream1, class OutputStream1>
                class Adapter {
                public:
                    
                    typedef Core::Templates::ReadVector<typename InputStream1::buffer_type, Extensions::Types::Complex> InputVector;
    
                    InputVector input(size_t count)
                    {
                            assert (_inputStream1.first != 0);
                            assert (count <= 1);
                            typename InputStream1::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _inputStream1.first->read(count, count, _inputStream1.second, data, offset, size);

                            return InputVector(data, count, offset, size, callback);
                    }

                    InputVector input(size_t count, size_t forward)
                    {
                            assert (_inputStream1.first != 0);
                            assert (count <= 1);

                            typename InputStream1::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _inputStream1.first->read(count, forward, _inputStream1.second, data, offset, size);

                            return InputVector(data, count, offset, size, callback);
                    }
                    
                    typedef Core::Templates::WriteVector<Extensions::Types::Complex, typename OutputStream1::buffer_type> OutputVector;
     
                    OutputVector output(size_t count, bool throwWhenStreamHasNoReaders=false)
                    {
                            assert (_outputStream1.first != 0);
                            assert (count <= 1);

                            typename OutputStream1::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _outputStream1.first->write(count, _outputStream1.second, throwWhenStreamHasNoReaders, data, offset, size);

                            return OutputVector(data, count, offset, size, callback);
                    }
                    

                    void addInputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                    {
               		 switch(streamId) {
            			
                    	case 0: 
                            {
                    		InputStream1* inputStream1 = dynamic_cast <InputStream1*>(stream); 
                   			assert(inputStream1);
                   			_inputStream1 = std::make_pair(inputStream1, moduleId);
                            }
                   			break;
                    			
                    	default:
                    		assert(false);
                    	}    
                    }
                    
                    void addOutputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                    {
                    	switch(streamId) {
            			
                    	case 0: 
                            {
                    		OutputStream1* outputStream1 = dynamic_cast <OutputStream1*>(stream); 
                   			assert(outputStream1);
                   			_outputStream1 = std::make_pair(outputStream1, moduleId);
                            }
                   			break;
                    			
                    	default:
                    		assert(false);
                    	}
                    }

                private:
                    std::pair<InputStream1*, unsigned int> _inputStream1;
                    std::pair<OutputStream1*, unsigned int> _outputStream1;
                    
                };

                enum Caller {
                	LETHE_SCHEDULER, INPUT, OUTPUT
                };

                Caller inputPortMap[] = {INPUT};
                Caller outputPortMap[] = {OUTPUT};
            }
        }
    }
}

#endif
