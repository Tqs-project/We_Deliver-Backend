package deti.tqs.webmarket.controller;

import deti.tqs.webmarket.dto.*;
import deti.tqs.webmarket.repository.UserRepository;
import deti.tqs.webmarket.service.RiderServiceImp;
import deti.tqs.webmarket.util.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Log4j2
@RestController
@RequestMapping("/api/riders")
public class RiderController {
    @Autowired
    private RiderServiceImp riderService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("")
    public ResponseEntity<RiderDto> createRider(@RequestBody RiderDto riderDto) {
        log.info("Saving rider " + riderDto.getUser().getUsername() + ".");
        return new ResponseEntity<>(riderService.registerRider(riderDto), HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<RiderFullInfoDto> getRider(@RequestHeader String username,
                                                     @RequestHeader String idToken) {
        var user = userRepository.findByUsername(username);
        if (user.isEmpty())
            return new ResponseEntity<>(new RiderFullInfoDto(), HttpStatus.UNAUTHORIZED);

        if (!idToken.equals(user.get().getAuthToken()))
            return new ResponseEntity<>(new RiderFullInfoDto(), HttpStatus.UNAUTHORIZED);

        return new ResponseEntity<>(
                Utils.parseRiderDto(user.get().getRider()),
                HttpStatus.OK
        );
    }


    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody CustomerLoginDto riderDto) {
        log.info("Logging in user");

        var rider = new RiderDto();
        var user = new UserDto();
        user.setUsername(riderDto.getUsername());
        user.setEmail(riderDto.getEmail());
        user.setPassword(riderDto.getPassword());
        rider.setUser(user);

        var response = this.riderService.login(rider);

        if (response.isEmpty())
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/ride/{id}/delivered")
    public ResponseEntity<String> updateOrderDelivered(@PathVariable Long id,
                                                       @RequestHeader String idToken,
                                                       @RequestHeader String username) {
        /**
         * updating the order to DELIVERED state
         * if the user is authenticated
         */

        var user = userRepository.findByUsername(username);
        if (user.isEmpty())
            return new ResponseEntity<>("Invalid username", HttpStatus.UNAUTHORIZED);

        if (!idToken.equals(user.get().getAuthToken()))
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);

        if (!riderService.updateOrderDelivered(id))
            return new ResponseEntity<>("No ride with id: " + id, HttpStatus.NOT_FOUND);

        log.info("The order with id " + id + " was delivered by " + username);

        return new ResponseEntity<>("Ride updated with success", HttpStatus.OK);
    }

    @GetMapping("/order")
    public ResponseEntity<OrderDto> getOrderAssigned(@RequestHeader String idToken,
                                     @RequestHeader String username) {

        var user = userRepository.findByUsername(username);
        if (user.isEmpty())
            return new ResponseEntity<>(new OrderDto(), HttpStatus.UNAUTHORIZED);

        if (!idToken.equals(user.get().getAuthToken()))
            return new ResponseEntity<>(new OrderDto(), HttpStatus.UNAUTHORIZED);

        if (!riderService.riderHasNewAssignment(username))
            return new ResponseEntity<>(new OrderDto(), HttpStatus.OK);

        return new ResponseEntity<>(
                riderService.retrieveOrderAssigned(username),
                HttpStatus.OK
        );
    }

    @PostMapping("/order/accept")
    public ResponseEntity<String> riderAcceptsTheOrderAssignedToHim(@RequestHeader String idToken,
                                                                    @RequestHeader String username) {
        var user = userRepository.findByUsername(username);
        if (user.isEmpty())
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);

        if (!idToken.equals(user.get().getAuthToken()))
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);

        this.riderService.riderAcceptsAssignment(username);
        log.info("Rider with username {" + username + "} accepted the order assigned to him.");
        return new ResponseEntity<>("Have a nice ride", HttpStatus.OK);
    }

    @PostMapping("/order/decline")
    public ResponseEntity<String> riderDeclinesTheOrderAssignedToHim(@RequestHeader String idToken,
                                                                     @RequestHeader String username) {
        var user = userRepository.findByUsername(username);
        if (user.isEmpty())
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);

        if (!idToken.equals(user.get().getAuthToken()))
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);

        this.riderService.riderDeclinesAssignment(username);
        log.info("Rider with username {" + username + "} does not accepted the order assigned to him.");
        return new ResponseEntity<>("No problem at all", HttpStatus.OK);
    }

    @PostMapping("/location")
    public ResponseEntity<String> updateRiderLocation(@RequestHeader String idToken,
                                                      @RequestHeader String username,
                                                      @RequestBody LocationDto location) {

        var user = userRepository.findByUsername(username);
        if (user.isEmpty())
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);

        if (!idToken.equals(user.get().getAuthToken()))
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);

        this.riderService.updateRiderLocation(user.get().getRider(), location);

        return new ResponseEntity<>("Location updated successfully.", HttpStatus.OK);
    }

    // TODO update de dados do rider

}
